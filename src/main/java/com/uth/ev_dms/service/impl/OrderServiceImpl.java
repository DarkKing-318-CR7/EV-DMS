package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.config.RabbitConfig;
import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.exception.BusinessException;
import com.uth.ev_dms.messaging.OrderApprovedEvent;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.InventoryService;
import com.uth.ev_dms.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final InventoryService inventoryService;
    private final OrderItemRepo orderItemRepo;
    private final QuoteRepo quoteRepo;
    private final UserRepo userRepo;

    // 🟢 Thêm MQ vào service
    private final RabbitTemplate rabbitTemplate;

    @Override
    public OrderHdr findById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    @Override
    public List<OrderItem> findItems(Long id) {
        return orderItemRepo.findByOrderId(id);
    }

    @Override
    @Transactional
    public OrderHdr createFromQuote(Long quoteId, Long dealerIdIgnored, Long customerIdIgnored, Long staffIdIgnored) {

        var quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        OrderHdr o = new OrderHdr();
        o.setQuoteId(quote.getId());
        o.setDealerId(quote.getDealerId());
        o.setCustomerId(quote.getCustomerId());

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                userRepo.findByUsername(auth.getName()).ifPresent(u -> {
                    o.setSalesStaffId(u.getId());
                });
            }
        } catch (Exception ignore) {}

        o.setOrderNo(generateOrderNo());
        o.setStatus(OrderStatus.NEW);

        BigDecimal total = quote.getFinalAmount() != null
                ? quote.getFinalAmount()
                : quote.getTotalAmount();
        if (total == null) total = BigDecimal.ZERO;

        o.setTotalAmount(total);
        o.setDepositAmount(BigDecimal.ZERO);
        o.setPaidAmount(BigDecimal.ZERO);
        o.setBalanceAmount(total);

        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public OrderHdr submitForAllocation(Long orderId) {
        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        if (o.getStatus() != OrderStatus.NEW) {
            throw new BusinessException("INVALID_STATE", "Only NEW orders can be submitted");
        }

        List<OrderItem> items = o.getItems();
        if (items == null || items.isEmpty()) {
            throw new BusinessException("NO_ITEMS", "Order has no items");
        }

        o.setStatus(OrderStatus.PENDING_ALLOC);

        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public OrderHdr allocate(Long orderId) {
        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));

        if (o.getStatus() != OrderStatus.PENDING_ALLOC) {
            throw new BusinessException("INVALID_STATE", "Order must be PENDING_ALLOC to allocate");
        }

        List<OrderItem> items = o.getItems();
        if (items == null || items.isEmpty()) {
            throw new BusinessException("NO_ITEMS", "Order has no items");
        }

        // 🔹 Allocate tồn kho
        inventoryService.allocateForOrder(orderId);

        // 🔹 Cập nhật trạng thái đơn
        o.setStatus(OrderStatus.ALLOCATED);
        if (o.getAllocatedAt() == null) {
            o.setAllocatedAt(LocalDateTime.now());
        }

        OrderHdr saved = orderRepo.save(o);

        // 🔹 User nhận thông báo = salesStaffId
        Long userId = saved.getSalesStaffId();

        // 🔹 Gửi MQ Event ORDER_APPROVED
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_ORDER,
                RabbitConfig.ROUTING_ORDER_APPROVED,
                new OrderApprovedEvent(saved.getId(), userId)
        );

        System.out.println("📤 MQ Event Sent: ORDER_APPROVED for orderId="
                + saved.getId() + ", userId=" + userId);

        return saved;
    }


    @Override
    @Transactional
    public OrderHdr markDelivered(Long orderId) {
        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        if (o.getStatus() != OrderStatus.ALLOCATED) {
            throw new BusinessException("INVALID_STATE", "Only ALLOCATED orders can be delivered");
        }

        BigDecimal total = o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount();
        BigDecimal paid  = o.getPaidAmount()  == null ? BigDecimal.ZERO : o.getPaidAmount();

        if (paid.compareTo(total) < 0) {
            throw new BusinessException("UNPAID",
                    "Không thể giao hàng: đơn chưa thanh toán đủ (" + paid + " / " + total + ")");
        }

        inventoryService.shipForOrder(orderId);

        o.setStatus(OrderStatus.DELIVERED);
        if (o.getDeliveredAt() == null) {
            o.setDeliveredAt(LocalDateTime.now());
        }
        return orderRepo.save(o);
    }

    @Override
    public List<OrderHdr> findMine(Long staffId) {
        return orderRepo.findBySalesStaffIdOrderByIdDesc(staffId);
    }

    @Override
    public List<OrderHdr> findAllForDealer(Long dealerId) {
        return orderRepo.findAllForDealer(dealerId);
    }

    private String generateOrderNo() {
        return "ODR-" + System.currentTimeMillis();
    }

    @Transactional
    public OrderHdr cancel(Long orderId) {
        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.ALLOCATED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel allocated or delivered order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepo.save(order);
    }

    @Transactional
    @Override
    public OrderHdr cancelByDealer(Long orderId, Long dealerId, Long actorId) {
        var o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        if (!o.getDealerId().equals(dealerId)) {
            throw new SecurityException("FORBIDDEN_DEALER");
        }

        if (o.getStatus() == OrderStatus.ALLOCATED) {
            throw new IllegalStateException("DEALLOCATE_FIRST");
        }
        if (o.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("DELIVERED_CANNOT_CANCEL");
        }

        o.setStatus(OrderStatus.CANCELLED);
        return orderRepo.save(o);
    }

    @Transactional
    @Override
    public OrderHdr deallocateByEvm(Long orderId, Long actorId, String reason) {
        var o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        if (o.getStatus() != OrderStatus.ALLOCATED) {
            throw new IllegalStateException("ONLY_ALLOCATED_CAN_BE_DEALLOCATED");
        }

        inventoryService.releaseForOrder(orderId);
        o.setStatus(OrderStatus.PENDING_ALLOC);

        return orderRepo.save(o);
    }

    @Transactional
    @Override
    public OrderHdr cancelByEvm(Long orderId, Long actorId, String reason) {
        var o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        if (o.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("DELIVERED_CANNOT_CANCEL");
        }
        if (o.getStatus() == OrderStatus.ALLOCATED) {
            throw new IllegalStateException("DEALLOCATE_FIRST");
        }
        if (o.getStatus() == OrderStatus.PENDING_ALLOC) {
            inventoryService.releaseForOrder(orderId);
        }

        o.setStatus(OrderStatus.CANCELLED);
        return orderRepo.save(o);
    }
}

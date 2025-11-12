package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.exception.BusinessException;
import com.uth.ev_dms.repo.OrderItemRepo;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.repo.PaymentRepo;
import com.uth.ev_dms.service.InventoryService;
import com.uth.ev_dms.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public OrderHdr createFromQuote(Long quoteId, Long dealerId, Long customerId, Long staffId) {
        OrderHdr o = new OrderHdr();
        o.setQuoteId(quoteId);
        o.setDealerId(dealerId);
        o.setCustomerId(customerId);
        o.setSalesStaffId(staffId);
        o.setOrderNo(generateOrderNo());
        o.setStatus(OrderStatus.NEW);

        if (o.getItems() != null && !o.getItems().isEmpty()) {
            BigDecimal total = o.getItems().stream()
                    .map(OrderItem::getLineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            o.setTotalAmount(total);
        } else {
            o.setTotalAmount(BigDecimal.ZERO);
        }

        o.setDepositAmount(o.getDepositAmount() == null ? BigDecimal.ZERO : o.getDepositAmount());
        o.setPaidAmount(BigDecimal.ZERO);
        o.setBalanceAmount(o.getTotalAmount().subtract(o.getDepositAmount()));
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

        inventoryService.allocateForOrder(orderId);

        o.setStatus(OrderStatus.ALLOCATED);
        // ✅ Ghi mốc thời gian allocate
        if (o.getAllocatedAt() == null) {
            o.setAllocatedAt(LocalDateTime.now());
        }
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public OrderHdr markDelivered(Long orderId) {
        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        if (o.getStatus() != OrderStatus.ALLOCATED) {
            throw new BusinessException("INVALID_STATE", "Only ALLOCATED orders can be delivered");
        }

        // ✅ CHẶN GIAO HÀNG KHI CHƯA THANH TOÁN ĐỦ
        BigDecimal total = o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount();
        BigDecimal paid  = o.getPaidAmount()  == null ? BigDecimal.ZERO : o.getPaidAmount();
        if (paid.compareTo(total) < 0) {
            // dùng BusinessException để controller hiển thị message đẹp
            throw new BusinessException(
                    "UNPAID",
                    "Không thể giao hàng: đơn chưa thanh toán đủ (" + paid + " / " + total + ")"
            );
        }

        inventoryService.shipForOrder(orderId);

        o.setStatus(OrderStatus.DELIVERED);
        // ✅ Ghi mốc thời gian delivered
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

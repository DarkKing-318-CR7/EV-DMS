package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.client.InventoryClient;
import com.uth.ev_dms.config.CacheConfig;
import com.uth.ev_dms.config.RabbitMQConfig;
import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.exception.BusinessException;
import com.uth.ev_dms.messaging.OrderApprovedEvent;
import com.uth.ev_dms.repo.OrderItemRepo;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.repo.PaymentRepo;
import com.uth.ev_dms.repo.QuoteRepo;
import com.uth.ev_dms.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final OrderItemRepo orderItemRepo;
    private final QuoteRepo quoteRepo;

    private final InventoryClient inventoryClient;   // <-- thay InventoryService bằng REST client
    private final RabbitTemplate rabbitTemplate;

    // ======================================================
    // ========================== GET ======================
    // ======================================================

    @Override
    @Cacheable(value = CacheConfig.CacheNames.ORDERS_MANAGER, key = "#id")
    public OrderHdr findById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    @Override
    @Cacheable(value = CacheConfig.CacheNames.ORDER_ITEMS, key = "#id")
    public List<OrderItem> findItems(Long id) {
        return orderItemRepo.findByOrderId(id);
    }

    @Override
    @Cacheable(value = CacheConfig.CacheNames.ORDERS_MY, key = "#staffId")
    public List<OrderHdr> findMine(Long staffId) {
        return orderRepo.findBySalesStaffIdOrderByIdDesc(staffId);
    }

    @Override
    @Cacheable(value = CacheConfig.CacheNames.ORDERS_DEALER, key = "#dealerId")
    public List<OrderHdr> findAllForDealer(Long dealerId) {
        return orderRepo.findAllForDealer(dealerId);
    }

    // ======================================================
    // ======================= CREATE =======================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.ORDERS_MY,
                    CacheConfig.CacheNames.ORDERS_DEALER,
                    CacheConfig.CacheNames.ORDERS_MANAGER,
                    CacheConfig.CacheNames.ORDER_ITEMS
            },
            allEntries = true
    )
    public OrderHdr createFromQuote(Long quoteId, Long dealerIdIgnore, Long customerIdIgnore, Long staffIdIgnore) {

        var quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        OrderHdr o = new OrderHdr();
        o.setQuoteId(quote.getId());
        o.setDealerId(quote.getDealerId());
        o.setCustomerId(quote.getCustomerId());

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

    // ======================================================
    // ==================== SUBMIT FOR ALLOC ================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.ORDERS_MANAGER,
                    CacheConfig.CacheNames.ORDERS_DEALER,
                    CacheConfig.CacheNames.ORDERS_MY
            },
            allEntries = true
    )
    public OrderHdr submitForAllocation(Long orderId) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));

        if (o.getStatus() != OrderStatus.NEW) {
            throw new BusinessException("INVALID_STATE", "Only NEW orders can be submitted");
        }

        if (o.getItems() == null || o.getItems().isEmpty()) {
            throw new BusinessException("NO_ITEMS", "Order has no items");
        }

        o.setStatus(OrderStatus.PENDING_ALLOC);
        return orderRepo.save(o);
    }

    // ======================================================
    // ======================== ALLOCATE ====================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.ORDERS_MANAGER,
                    CacheConfig.CacheNames.ORDERS_MY,
                    CacheConfig.CacheNames.ORDERS_DEALER,
                    CacheConfig.CacheNames.ORDER_ITEMS
            },
            allEntries = true
    )
    public OrderHdr allocate(Long orderId) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));

        if (o.getStatus() != OrderStatus.PENDING_ALLOC) {
            throw new BusinessException("INVALID_STATE", "Order must be PENDING_ALLOC");
        }

        if (o.getItems() == null || o.getItems().isEmpty()) {
            throw new BusinessException("NO_ITEMS", "Order has no items");
        }

        // REST call sang inventory-service
        inventoryClient.allocate(orderId);

        o.setStatus(OrderStatus.ALLOCATED);
        if (o.getAllocatedAt() == null) o.setAllocatedAt(LocalDateTime.now());

        OrderHdr saved = orderRepo.save(o);

        // gửi event MQ
        OrderApprovedEvent event = new OrderApprovedEvent(saved.getId(), saved.getSalesStaffId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ROUTING_ORDER_APPROVED,
                event
        );

        System.out.println("📤 Event ORDER_APPROVED sent: orderId="
                + saved.getId() + ", staff=" + saved.getSalesStaffId());

        return saved;
    }

    // ======================================================
    // ======================== DELIVER ======================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.ORDERS_MANAGER,
                    CacheConfig.CacheNames.ORDERS_DEALER,
                    CacheConfig.CacheNames.ORDERS_MY
            },
            allEntries = true
    )
    public OrderHdr markDelivered(Long orderId) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));

        if (o.getStatus() != OrderStatus.ALLOCATED) {
            throw new BusinessException("INVALID_STATE", "Must be ALLOCATED");
        }

        BigDecimal total = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid  = o.getPaidAmount()  != null ? o.getPaidAmount() : BigDecimal.ZERO;

        if (paid.compareTo(total) < 0) {
            throw new BusinessException("UNPAID", "Chưa thanh toán đủ");
        }

        inventoryClient.ship(orderId);

        o.setStatus(OrderStatus.DELIVERED);
        if (o.getDeliveredAt() == null) o.setDeliveredAt(LocalDateTime.now());

        return orderRepo.save(o);
    }

    // ======================================================
    // ========================= CANCEL ======================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.ORDERS_MANAGER,
                    CacheConfig.CacheNames.ORDERS_MY,
                    CacheConfig.CacheNames.ORDERS_DEALER
            },
            allEntries = true
    )
    public OrderHdr cancel(Long orderId) {

        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.ALLOCATED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Không thể hủy đơn đã cấp hoặc đã giao");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepo.save(order);
    }

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.ORDERS_MANAGER,
                    CacheConfig.CacheNames.ORDERS_MY,
                    CacheConfig.CacheNames.ORDERS_DEALER
            },
            allEntries = true
    )
    public OrderHdr cancelByDealer(Long orderId, Long dealerId, Long actorId) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        if (!o.getDealerId().equals(dealerId)) {
            throw new SecurityException("FORBIDDEN");
        }

        if (o.getStatus() == OrderStatus.ALLOCATED)
            throw new IllegalStateException("DEALLOCATE_FIRST");

        if (o.getStatus() == OrderStatus.DELIVERED)
            throw new IllegalStateException("DELIVERED_CANNOT_CANCEL");

        o.setStatus(OrderStatus.CANCELLED);
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.ORDERS_MANAGER,
                    CacheConfig.CacheNames.ORDERS_MY,
                    CacheConfig.CacheNames.ORDERS_DEALER
            },
            allEntries = true
    )
    public OrderHdr deallocateByEvm(Long orderId, Long actorId, String reason) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        if (o.getStatus() != OrderStatus.ALLOCATED) {
            throw new IllegalStateException("ONLY_ALLOCATED");
        }

        inventoryClient.release(orderId);

        o.setStatus(OrderStatus.PENDING_ALLOC);
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.ORDERS_MANAGER,
                    CacheConfig.CacheNames.ORDERS_MY,
                    CacheConfig.CacheNames.ORDERS_DEALER
            },
            allEntries = true
    )
    public OrderHdr cancelByEvm(Long orderId, Long actorId, String reason) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        if (o.getStatus() == OrderStatus.DELIVERED)
            throw new IllegalStateException("DELIVERED_CANNOT_CANCEL");

        if (o.getStatus() == OrderStatus.ALLOCATED)
            throw new IllegalStateException("DEALLOCATE_FIRST");

        if (o.getStatus() == OrderStatus.PENDING_ALLOC)
            inventoryClient.release(orderId);

        o.setStatus(OrderStatus.CANCELLED);
        return orderRepo.save(o);
    }

    // ======================================================
    // ========================= UTIL =======================
    // ======================================================

    private String generateOrderNo() {
        return "ODR-" + System.currentTimeMillis();
    }
}

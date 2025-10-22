package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.exception.BusinessException;
import com.uth.ev_dms.repo.OrderItemRepo;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.repo.PaymentRepo;
import com.uth.ev_dms.service.OrderService;
import com.uth.ev_dms.service.InventoryService; // assume you have this in Part 1
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    }// hook to Part 1

    @Override
    @Transactional
    public OrderHdr createFromQuote(Long quoteId, Long dealerId, Long customerId, Long staffId) {
        // TODO: load quote and copy items; here a minimal skeleton:
        OrderHdr o = new OrderHdr();
        o.setQuoteId(quoteId);
        o.setDealerId(dealerId);
        o.setCustomerId(customerId);
        o.setSalesStaffId(staffId);
        o.setOrderNo(generateOrderNo());
        o.setStatus(OrderStatus.NEW);

        // calculate totals from items (assuming items already attached elsewhere)
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
        OrderHdr o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        if (o.getStatus() != OrderStatus.NEW) {
            throw new BusinessException("INVALID_STATE", "Only NEW orders can be submitted");
        }
        o.setStatus(OrderStatus.PENDING_ALLOC);
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public OrderHdr allocate(Long orderId) {
        OrderHdr o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        if (o.getStatus() != OrderStatus.PENDING_ALLOC) {
            throw new BusinessException("INVALID_STATE", "Order must be PENDING_ALLOC to allocate");
        }
        AtomicInteger shortage = new AtomicInteger(0);
        if (o.getItems() == null || o.getItems().isEmpty()) {
            throw new BusinessException("NO_ITEMS", "Order has no items");
        }
        o.getItems().forEach(it -> {
            boolean ok = inventoryService.allocateForOrder(it); // ✅ gọi đúng chữ ký 1 tham số
            if (!ok) shortage.incrementAndGet();
        });
        if (shortage.get() > 0) {
            throw new BusinessException("ALLOCATION_SHORTAGE", "Insufficient inventory for one or more items");
        }
        o.setStatus(OrderStatus.ALLOCATED);
        return orderRepo.save(o);

    }

    @Override
    @Transactional
    public OrderHdr markDelivered(Long orderId) {
        OrderHdr o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        if (o.getStatus() != OrderStatus.ALLOCATED) {
            throw new BusinessException("INVALID_STATE", "Only ALLOCATED orders can be delivered");
        }
        // Optional: check fully paid
        o.setStatus(OrderStatus.DELIVERED);
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
}

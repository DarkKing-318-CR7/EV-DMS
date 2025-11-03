package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import com.uth.ev_dms.service.dto.CreateQuoteItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesServiceImpl implements SalesService {

    private final QuoteRepo quoteRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final PaymentRepo paymentRepo;
    private final PromotionRepo promotionRepo;
    private final PromotionService promotionService; // ✅ thêm đúng chỗ

    // ✅ Constructor đầy đủ dependencies
    public SalesServiceImpl(
            QuoteRepo quoteRepo,
            OrderRepo orderRepo,
            OrderItemRepo orderItemRepo,
            PaymentRepo paymentRepo,
            PromotionRepo promotionRepo,
            PromotionService promotionService // ✅ thêm vào constructor
    ) {
        this.quoteRepo = quoteRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
        this.promotionRepo = promotionRepo;
        this.promotionService = promotionService; // ✅ gán vào
    }

    // ======================= CREATE QUOTE =======================
    @Override
    @Transactional
    public Quote createQuote(CreateQuoteDTO dto) {
        Quote q = new Quote();
        q.setCustomerId(dto.getCustomerId());
        q.setStatus("DRAFT");
        q.setTotalAmount(dto.getTotalAmount());
        q.setAppliedDiscount(BigDecimal.ZERO);
        q.setFinalAmount(dto.getTotalAmount());

        // Tạo danh sách item
        List<QuoteItem> items = new ArrayList<>();
        if (dto.getItems() != null) {
            for (CreateQuoteItemDTO it : dto.getItems()) {
                QuoteItem qi = new QuoteItem();
                qi.setVehicleId(it.getVehicleId());
                qi.setQuantity(it.getQuantity());
                qi.setUnitPrice(it.getUnitPrice());
                qi.setQuote(q);
                items.add(qi);
            }
        }
        q.setItems(items);

        return quoteRepo.save(q);
    }

    // ======================= APPLY PROMOTIONS =======================
    @Override
    @Transactional
    public Quote applyPromotions(Long quoteId, List<Long> promotionIds) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        if (promotionIds == null || promotionIds.isEmpty()) {
            quote.setAppliedDiscount(BigDecimal.ZERO);
            quote.setFinalAmount(quote.getTotalAmount());
            return quoteRepo.save(quote);
        }

        // ✅ Gọi PromotionService để tính tổng giảm
        BigDecimal discount = promotionService.computeDiscount(quote.getTotalAmount(), promotionIds);

        // ✅ Tính lại giá cuối
        BigDecimal finalAmount = quote.getTotalAmount().subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        quote.setAppliedDiscount(discount);
        quote.setFinalAmount(finalAmount);

        System.out.println("✅ Quote " + quoteId + " applied promotions " + promotionIds
                + " → discount = " + discount + ", final = " + finalAmount);

        return quoteRepo.save(quote);
    }

    // ======================= APPROVE QUOTE =======================
    @Override
    @Transactional
    public OrderHdr approveQuote(Long quoteId) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        quote.setStatus("APPROVED");
        quoteRepo.save(quote);

        OrderHdr order = new OrderHdr();
        order.setQuoteId(quote.getId());
        order.setOrderNo("ORD-" + java.time.LocalDate.now() + "-" + quote.getId());
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(quote.getFinalAmount()); // ✅ dùng finalAmount sau giảm
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setDepositAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setBalanceAmount(order.getTotalAmount());

        OrderHdr savedOrder = orderRepo.save(order);

        if (quote.getItems() != null) {
            for (QuoteItem qi : quote.getItems()) {
                OrderItem oi = new OrderItem();
                oi.setOrder(savedOrder);
                oi.setTrimId(qi.getVehicleId());
                oi.setQty(qi.getQuantity());
                oi.setUnitPrice(qi.getUnitPrice());
                orderItemRepo.save(oi);
            }
        }

        return savedOrder;
    }

    // ======================= SUBMIT & REJECT =======================
    @Override
    @Transactional
    public Quote submitQuote(Long quoteId) {
        Quote q = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));
        q.setStatus("PENDING");
        return quoteRepo.save(q);
    }

    @Override
    @Transactional
    public Quote rejectQuote(Long quoteId, String comment) {
        Quote q = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));
        q.setStatus("REJECTED");
        q.setRejectComment(comment);
        return quoteRepo.save(q);
    }

    // ======================= FIND =======================
    @Override
    public List<Quote> findPending() {
        return quoteRepo.findByStatus("PENDING");
    }

    @Override
    public List<Quote> findAll() {
        return quoteRepo.findAll();
    }

    // ======================= PAYMENT =======================
    @Override
    @Transactional
    public Payment makeCashPayment(Long orderId, BigDecimal amount) {
        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Payment p = new Payment();
        p.setOrder(order);
        p.setType(PaymentType.CASH);
        p.setAmount(amount);
        return paymentRepo.save(p);
    }

    @Override
    @Transactional
    public Payment makeInstallmentPayment(Long orderId, BigDecimal amount) {
        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Payment p = new Payment();
        p.setOrder(order);
        p.setType(PaymentType.INSTALLMENT);
        p.setAmount(amount);
        return paymentRepo.save(p);
    }
}

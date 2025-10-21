package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import com.uth.ev_dms.service.dto.CreateQuoteItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesServiceImpl implements SalesService {

    private final QuoteRepo quoteRepo;
    private final QuoteItemRepo quoteItemRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final PaymentRepo paymentRepo;
    private final PromotionService promotionService;
    private final PromotionAppliedRepo promotionAppliedRepo;

    public SalesServiceImpl(
            QuoteRepo quoteRepo,
            QuoteItemRepo quoteItemRepo,
            OrderRepo orderRepo,
            OrderItemRepo orderItemRepo,
            PaymentRepo paymentRepo,
            PromotionService promotionService,
            PromotionAppliedRepo promotionAppliedRepo
    ) {
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
        this.promotionService = promotionService;
        this.promotionAppliedRepo = promotionAppliedRepo;
    }

    // ==================== QUOTE CREATION ====================
    @Override
    @Transactional
    public Quote createQuote(CreateQuoteDTO dto) {
        Quote q = new Quote();
        q.setCustomerId(dto.getCustomerId());
        q.setStatus("DRAFT");

        BigDecimal total = BigDecimal.ZERO;
        List<QuoteItem> items = new ArrayList<>();

        if (dto.getItems() != null) {
            for (CreateQuoteItemDTO it : dto.getItems()) {
                QuoteItem qi = new QuoteItem();
                qi.setVehicleId(it.getVehicleId());
                qi.setQuantity(it.getQuantity());
                qi.setUnitPrice(it.getUnitPrice());
                qi.setQuote(q);
                items.add(qi);

                total = total.add(it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())));
            }
        }

        q.setItems(items);
        q.setTotalAmount(total);
        return quoteRepo.save(q);
    }

    // ==================== QUOTE WORKFLOW ====================

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

    @Override
    @Transactional
    public OrderHdr approveQuote(Long quoteId) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        // Cập nhật trạng thái quote
        quote.setStatus("APPROVED");
        quoteRepo.save(quote);

        // Tạo order mới dựa trên quote được duyệt
        OrderHdr order = new OrderHdr();
        order.setQuoteId(quote.getId());
        order.setStatus("PENDING");
        order.setTotalAmount(quote.getFinalAmount() != null ? quote.getFinalAmount() : quote.getTotalAmount());
        order.setCreatedAt(LocalDateTime.now());

        OrderHdr savedOrder = orderRepo.save(order);

        // Chuyển từng QuoteItem -> OrderItem
        for (QuoteItem qi : quote.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setVehicleId(qi.getVehicleId());
            oi.setQuantity(qi.getQuantity());
            oi.setUnitPrice(qi.getUnitPrice());
            orderItemRepo.save(oi);
        }

        return savedOrder;
    }


    @Override
    public List<Quote> findPending() {
        return quoteRepo.findByStatus("PENDING");
    }

    @Override
    public List<Quote> findAll() {
        return quoteRepo.findAll();
    }

    // ==================== PROMOTION APPLY ====================
    @Override
    @Transactional
    public Quote applyPromotions(Long quoteId, List<Long> promotionIds) {
        Quote q = quoteRepo.findById(quoteId).orElseThrow();

        var today = LocalDate.now();
        var valid = promotionService.getValidPromotions(
                        q.getDealerId(), q.getVehicleTrimId(), q.getRegion(), today)
                .stream().map(Promotion::getId).collect(Collectors.toSet());

        var toApply = (promotionIds == null)
                ? List.<Long>of()
                : promotionIds.stream().filter(valid::contains).toList();

        var discount = promotionService.computeDiscount(q.getTotalAmount(), toApply);
        q.setAppliedDiscount(discount);
        q.setFinalAmount(q.getTotalAmount().subtract(discount).max(BigDecimal.ZERO));
        quoteRepo.save(q);

        for (Long pid : toApply) {
            PromotionApplied pa = new PromotionApplied();
            pa.setQuoteId(q.getId());
            pa.setPromotionId(pid);
            pa.setDiscountAmount(null);
            pa.setAppliedAt(LocalDateTime.now());
            promotionAppliedRepo.save(pa);
        }
        return q;
    }

    // ==================== PAYMENT ====================
    @Override
    @Transactional
    public Payment makeCashPayment(Long orderId, BigDecimal amount) {
        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Payment p = new Payment();
        p.setOrderId(order.getId());
        p.setPaymentType("cash");
        p.setAmount(amount);
        return paymentRepo.save(p);
    }

    @Override
    @Transactional
    public Payment makeInstallmentPayment(Long orderId, BigDecimal amount) {
        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setPaymentType("installment");
        p.setAmount(amount);
        return paymentRepo.save(p);
    }
}

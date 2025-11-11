package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.PromotionService;
import com.uth.ev_dms.service.SalesService;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import com.uth.ev_dms.service.dto.CreateQuoteItemDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PromotionService promotionService;

    // repos phục vụ backfill khi approve
    private final CustomerRepo customerRepo;
    private final UserRepo userRepo;

    public SalesServiceImpl(
            QuoteRepo quoteRepo,
            OrderRepo orderRepo,
            OrderItemRepo orderItemRepo,
            PaymentRepo paymentRepo,
            PromotionRepo promotionRepo,          // giữ nguyên tham số cũ
            PromotionService promotionService,
            CustomerRepo customerRepo,
            UserRepo userRepo
    ) {
        this.quoteRepo = quoteRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
        this.promotionService = promotionService;
        this.customerRepo = customerRepo;
        this.userRepo = userRepo;
    }

    // ======================= CREATE QUOTE =======================
    @Override
    @Transactional
    public Quote createQuote(CreateQuoteDTO dto) {
        Quote q = new Quote();
        q.setCustomerId(dto.getCustomerId());
        q.setDealerId(dto.getDealerId()); // có thể null — sẽ auto fill bên dưới
        q.setStatus("DRAFT");

        // ===== NULL-safe total/final
        BigDecimal total = dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO;
        q.setTotalAmount(total);
        q.setAppliedDiscount(BigDecimal.ZERO);
        q.setFinalAmount(total);

        // ===== Lấy user hiện tại để fill dealerId & owner khách (nếu thiếu)
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                userRepo.findByUsername(auth.getName()).ifPresent(u -> {
                    // Nếu quote chưa có dealerId, set theo dealer của user hiện tại
                    if (q.getDealerId() == null && u.getDealer() != null) {
                        q.setDealerId(u.getDealer().getId());
                    }
                    // Nếu customer chưa có owner → gán owner = user hiện tại
                    if (dto.getCustomerId() != null) {
                        customerRepo.findById(dto.getCustomerId()).ifPresent(c -> {
                            if (c.getOwnerId() == null) {
                                c.setOwnerId(u.getId());
                                customerRepo.save(c);
                            }
                        });
                    }
                });
            }
        } catch (Exception ignore) {}

        // ===== Items
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

        BigDecimal discount = promotionService.computeDiscount(quote.getTotalAmount(), promotionIds);
        BigDecimal finalAmount = quote.getTotalAmount().subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

        quote.setAppliedDiscount(discount);
        quote.setFinalAmount(finalAmount);
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
        order.setCreatedAt(java.time.LocalDateTime.now());

        // 1) Điền từ quote
        order.setCustomerId(quote.getCustomerId());
        order.setDealerId(quote.getDealerId()); // có thể còn null

        // 2) Ưu tiên chủ khách hàng (owner) làm sales; và lấy dealer theo owner nếu thiếu
        customerRepo.findById(quote.getCustomerId()).ifPresent(c -> {
            order.setCustomerName(c.getTen());
            if (order.getSalesStaffId() == null) {
                order.setSalesStaffId(c.getOwnerId()); // có thể null nếu chưa gán owner
            }
            if (order.getDealerId() == null && c.getOwnerId() != null) {
                userRepo.findById(c.getOwnerId()).ifPresent(u -> {
                    if (u.getDealer() != null) order.setDealerId(u.getDealer().getId());
                });
            }
        });

        // 3) Fallback: user đang duyệt (tránh để NULL)
        if (order.getSalesStaffId() == null || order.getDealerId() == null) {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    userRepo.findByUsername(auth.getName()).ifPresent(u -> {
                        if (order.getSalesStaffId() == null) {
                            order.setSalesStaffId(u.getId());
                        }
                        if (order.getDealerId() == null && u.getDealer() != null) {
                            order.setDealerId(u.getDealer().getId());
                        }
                    });
                }
            } catch (Exception ignore) {}
        }

        // 4) Null-safe tiền
        BigDecimal ZERO = BigDecimal.ZERO;
        BigDecimal total = quote.getFinalAmount();
        if (total == null || total.compareTo(ZERO) < 0) {
            total = (quote.getTotalAmount() != null) ? quote.getTotalAmount() : ZERO;
        }
        order.setTotalAmount(total);
        order.setDepositAmount(ZERO);
        order.setPaidAmount(ZERO);
        order.setBalanceAmount(total);

        // Lưu header
        OrderHdr savedOrder = orderRepo.save(order);

        // Copy item từ quote sang order
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

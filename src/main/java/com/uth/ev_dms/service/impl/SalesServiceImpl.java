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
    private final QuoteItemRepo quoteItemRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final PaymentRepo paymentRepo;
    private final PromotionService promotionService;

    private final CustomerRepo customerRepo;
    private final UserRepo userRepo;

    public SalesServiceImpl(
            QuoteRepo quoteRepo,
            QuoteItemRepo quoteItemRepo,
            OrderRepo orderRepo,
            OrderItemRepo orderItemRepo,
            PaymentRepo paymentRepo,
            PromotionRepo promotionRepo,            // giữ nguyên để không phá DI khác
            PromotionService promotionService,
            CustomerRepo customerRepo,
            UserRepo userRepo
    ) {
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
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
        q.setDealerId(dto.getDealerId());
        q.setStatus("DRAFT");

        // Lấy user hiện tại để backfill dealer/owner/salesStaff
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                userRepo.findByUsername(auth.getName()).ifPresent(u -> {
                    // 👇 staff tạo quote
                    q.setSalesStaffId(u.getId());

                    if (q.getDealerId() == null && u.getDealer() != null) {
                        q.setDealerId(u.getDealer().getId());
                    }
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

        // Build items từ DTO
        List<QuoteItem> items = new ArrayList<>();
        if (dto.getItems() != null) {
            for (CreateQuoteItemDTO it : dto.getItems()) {
                if (it == null) continue;
                Integer qty = it.getQuantity();
                BigDecimal unit = it.getUnitPrice();
                Long trimId = it.getVehicleId(); // trim_id

                if (trimId == null || qty == null || qty <= 0) continue;
                if (unit == null) unit = BigDecimal.ZERO;

                QuoteItem qi = new QuoteItem();
                qi.setVehicleId(trimId);
                qi.setQuantity(qty);
                qi.setUnitPrice(unit);
                qi.setQuote(q);

                items.add(qi);
            }
        }
        q.setItems(items);

        BigDecimal total = dto.getTotalAmount();
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            total = items.stream()
                    .map(x -> x.getUnitPrice().multiply(BigDecimal.valueOf(x.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        q.setTotalAmount(total);
        q.setAppliedDiscount(BigDecimal.ZERO);
        q.setFinalAmount(total);

        Quote saved = quoteRepo.save(q);

        if (!items.isEmpty()) {
            quoteItemRepo.saveAll(items);
        }

        return saved;
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

        // Bảo vệ: quote phải có item
        if (quote.getItems() == null || quote.getItems().isEmpty()) {
            throw new IllegalStateException("Order has no items");
        }

        quote.setStatus("APPROVED");
        quoteRepo.save(quote);

        OrderHdr order = new OrderHdr();
        order.setQuoteId(quote.getId());
        order.setOrderNo("ORD-" + java.time.LocalDate.now() + "-" + quote.getId());
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(java.time.LocalDateTime.now());

        order.setCustomerId(quote.getCustomerId());
        order.setDealerId(quote.getDealerId());

        // 🔹 ƯU TIÊN: salesStaff từ quote (staff tạo quote)
        order.setSalesStaffId(quote.getSalesStaffId());

        // Thông tin customer + fallback nếu thiếu sales/dealer
        if (quote.getCustomerId() != null) {
            customerRepo.findById(quote.getCustomerId()).ifPresent(c -> {
                order.setCustomerName(c.getTen());

                // nếu quote không set sales_staff_id thì dùng owner
                if (order.getSalesStaffId() == null) {
                    order.setSalesStaffId(c.getOwnerId());
                }

                // nếu dealerId vẫn null thì lấy theo dealer của owner
                if (order.getDealerId() == null && c.getOwnerId() != null) {
                    userRepo.findById(c.getOwnerId()).ifPresent(u -> {
                        if (u.getDealer() != null) {
                            order.setDealerId(u.getDealer().getId());
                        }
                    });
                }
            });
        }

        // Fallback cuối cùng: user hiện tại (manager) nếu vẫn thiếu
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

        // Tiền
        BigDecimal ZERO = BigDecimal.ZERO;
        BigDecimal total = quote.getFinalAmount();
        if (total == null || total.compareTo(ZERO) < 0) {
            total = (quote.getTotalAmount() != null) ? quote.getTotalAmount() : ZERO;
        }
        order.setTotalAmount(total);
        order.setDepositAmount(ZERO);
        order.setPaidAmount(ZERO);
        order.setBalanceAmount(total);

        OrderHdr savedOrder = orderRepo.save(order);

        // Copy items sang order
        for (QuoteItem qi : quote.getItems()) {
            if (qi.getVehicleId() == null || qi.getQuantity() == null || qi.getQuantity() <= 0) continue;

            BigDecimal unit = qi.getUnitPrice() != null ? qi.getUnitPrice() : ZERO;
            int qty = qi.getQuantity();
            BigDecimal line = unit.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setTrimId(qi.getVehicleId());   // vehicleId = trim_id
            oi.setQty(qty);
            oi.setUnitPrice(unit);
            oi.setLineAmount(line);
            orderItemRepo.save(oi);
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

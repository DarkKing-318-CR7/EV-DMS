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
import java.time.LocalDate;
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
    private final TrimRepo trimRepo;
    private final InventoryRepo inventoryRepo;

    // FULL Constructor
    public SalesServiceImpl(
            QuoteRepo quoteRepo,
            QuoteItemRepo quoteItemRepo,
            OrderRepo orderRepo,
            OrderItemRepo orderItemRepo,
            PaymentRepo paymentRepo,
            PromotionService promotionService,
            CustomerRepo customerRepo,
            UserRepo userRepo,
            TrimRepo trimRepo,
            InventoryRepo inventoryRepo
    ) {
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
        this.promotionService = promotionService;
        this.customerRepo = customerRepo;
        this.userRepo = userRepo;
        this.trimRepo = trimRepo;
        this.inventoryRepo = inventoryRepo;
    }

    // ==============================
    // CREATE QUOTE WITH INVENTORY CHECK
    // ==============================
    @Override
    @Transactional
    public Quote createQuote(CreateQuoteDTO dto) {
        Quote q = new Quote();
        q.setCustomerId(dto.getCustomerId());
        q.setStatus(dto.getStatus() != null ? dto.getStatus() : "DRAFT");

        // ====== GET CURRENT USER & DEALER ======
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            userRepo.findByUsername(auth.getName()).ifPresent(u -> {
                if (u.getDealer() == null) {
                    throw new IllegalStateException("User không thuộc dealer nào → Không thể tạo quote");
                }

                // dealer hiện tại
                q.setDealerId(u.getDealer().getId());

                // 👇 GHI NHẬN STAFF TẠO BÁO GIÁ
                q.setSalesStaffId(u.getId());
                q.setCreatedBy(u.getId());

                // nếu customer chưa có owner thì gán luôn
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

        Quote saved = quoteRepo.save(q);

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Quote must contain at least one item.");
        }

        BigDecimal total = BigDecimal.ZERO;

        // ====== LOOP OVER ITEMS ======
        for (CreateQuoteItemDTO item : dto.getItems()) {
            if (item.getTrimId() == null || item.getQuantity() == null) continue;

            // ====== INVENTORY CHECK ======
            Integer available = inventoryRepo.sumQtyByTrimAndDealer(item.getTrimId(), q.getDealerId());
            if (available == null) available = 0;

            if (item.getQuantity() > available) {
                throw new IllegalArgumentException(
                        "Not enough stock for TrimID " + item.getTrimId() +
                                " | Requested: " + item.getQuantity() +
                                " | Available: " + available
                );
            }

            // ====== LOAD TRIM ======
            Trim trim = trimRepo.findById(item.getTrimId())
                    .orElseThrow(() -> new IllegalArgumentException("Trim not found: " + item.getTrimId()));

            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : trim.getCurrentPrice();
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // ====== SAVE QUOTE ITEM ======
            QuoteItem qi = new QuoteItem();
            qi.setQuote(saved);
            qi.setTrimId(item.getTrimId());
            qi.setQuantity(item.getQuantity());
            qi.setUnitPrice(unitPrice);
            qi.setLineAmount(amount);

            quoteItemRepo.save(qi);

            total = total.add(amount);
        }

        saved.setTotalAmount(total);
        saved.setFinalAmount(total);
        return quoteRepo.save(saved);
    }

    // ==============================
    // APPLY PROMOTION
    // ==============================
    @Override
    @Transactional
    public Quote applyPromotions(Long quoteId, List<Long> promotionIds) {
        Quote q = quoteRepo.findById(quoteId).orElseThrow(() ->
                new RuntimeException("Quote not found: " + quoteId));

        if (q.getTotalAmount() == null)
            throw new RuntimeException("Quote totalAmount missing");

        BigDecimal discount = promotionService.computeDiscountForQuote(
                q.getTotalAmount(),
                promotionIds,
                q.getDealerId(),
                q.getVehicleTrimId(),
                q.getRegion(),
                LocalDate.now()
        );

        q.setAppliedDiscount(discount);
        q.setFinalAmount(q.getTotalAmount().subtract(discount));

        return quoteRepo.save(q);
    }

    // ==============================
    // APPROVE → CREATE ORDER
    // ==============================
    @Override
    @Transactional
    public OrderHdr approveQuote(Long quoteId) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        if (quote.getItems() == null || quote.getItems().isEmpty()) {
            throw new IllegalStateException("Order has no items");
        }

        quote.setStatus("APPROVED");
        quoteRepo.save(quote);

        OrderHdr order = new OrderHdr();
        order.setQuoteId(quote.getId());
        order.setOrderNo("ORD-" + LocalDate.now() + "-" + quote.getId());
        order.setStatus(OrderStatus.NEW);
        order.setCustomerId(quote.getCustomerId());
        order.setDealerId(quote.getDealerId());
        order.setCreatedAt(java.time.LocalDateTime.now());

        BigDecimal total = quote.getFinalAmount() != null ? quote.getFinalAmount() : quote.getTotalAmount();
        total = total != null ? total : BigDecimal.ZERO;

        order.setTotalAmount(total);
        order.setDepositAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setBalanceAmount(total);

        // 👇 GÁN LẠI ĐÚNG STAFF TẠO BÁO GIÁ
        order.setSalesStaffId(quote.getSalesStaffId());
        order.setCreatedBy(quote.getCreatedBy());

        OrderHdr savedOrder = orderRepo.save(order);

        for (QuoteItem qi : quote.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setTrimId(qi.getTrimId());
            oi.setQty(qi.getQuantity());
            oi.setUnitPrice(qi.getUnitPrice());
            oi.setLineAmount(qi.getLineAmount());
            orderItemRepo.save(oi);
        }

        return savedOrder;
    }

    // ==============================
    // CHANGE STATUS
    // ==============================
    @Override
    @Transactional
    public Quote submitQuote(Long quoteId) {
        Quote q = quoteRepo.findById(quoteId).orElseThrow();
        q.setStatus("PENDING");
        return quoteRepo.save(q);
    }

    @Override
    @Transactional
    public Quote rejectQuote(Long quoteId, String comment) {
        Quote q = quoteRepo.findById(quoteId).orElseThrow();
        q.setStatus("REJECTED");
        q.setRejectComment(comment);
        return quoteRepo.save(q);
    }

    // ==============================
    // FIND METHODS
    // ==============================
    @Override
    public List<Quote> findPending() { return quoteRepo.findByStatus("PENDING"); }

    @Override
    public List<Quote> findAll() { return quoteRepo.findAll(); }

    // ==============================
    // PAYMENT
    // ==============================
    @Override
    @Transactional
    public Payment makeCashPayment(Long orderId, BigDecimal amount) {
        OrderHdr order = orderRepo.findById(orderId).orElseThrow();
        Payment p = new Payment();
        p.setOrder(order);
        p.setType(PaymentType.CASH);
        p.setAmount(amount);
        return paymentRepo.save(p);
    }

    @Override
    @Transactional
    public Payment makeInstallmentPayment(Long orderId, BigDecimal amount) {
        OrderHdr order = orderRepo.findById(orderId).orElseThrow();
        Payment p = new Payment();
        p.setOrder(order);
        p.setType(PaymentType.INSTALLMENT);
        p.setAmount(amount);
        return paymentRepo.save(p);
    }
}

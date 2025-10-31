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

    public SalesServiceImpl(QuoteRepo quoteRepo,
                            OrderRepo orderRepo,
                            OrderItemRepo orderItemRepo,
                            PaymentRepo paymentRepo) {
        this.quoteRepo = quoteRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.paymentRepo = paymentRepo;
    }

    @Override
    @Transactional
    public Quote createQuote(CreateQuoteDTO dto) {
        Quote q = new Quote();
        q.setCustomerId(dto.getCustomerId());
        q.setStatus("DRAFT");
        q.setTotalAmount(dto.getTotalAmount());
        q.setAppliedDiscount(BigDecimal.ZERO);
        q.setFinalAmount(dto.getTotalAmount());
        // tao danh sach item
        List<QuoteItem> items = new ArrayList<>();
        if (dto.getItems() != null) {
            for (CreateQuoteItemDTO it : dto.getItems()) {
                QuoteItem qi = new QuoteItem();
                qi.setVehicleId(it.getVehicleId());
                qi.setQuantity(it.getQuantity());
                qi.setUnitPrice(it.getUnitPrice());
                qi.setQuote(q);          // lien ket 1-nhieu
                items.add(qi);
            }
        }
        q.setItems(items);

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

        // Tạo mã đơn tự động (ví dụ: ORD-20251030-00011)
        String orderNo = "ORD-" + java.time.LocalDate.now() + "-" + quote.getId();

        // Tạo order từ quote
        OrderHdr order = new OrderHdr();
        order.setQuoteId(quote.getId());
        order.setOrderNo(orderNo); // ✅ thêm dòng này
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(quote.getTotalAmount());

        // Gán thêm các field bắt buộc nếu có
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setDepositAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setBalanceAmount(order.getTotalAmount());

        OrderHdr savedOrder = orderRepo.save(order);

        // Copy items từ Quote sang Order
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
    public List<Quote> findPending() {
        return quoteRepo.findByStatus("PENDING"); // ✅ Dành cho Manager
    }
    @Override
    public List<Quote> findAll() {
        return quoteRepo.findAll(); // ✅ Lấy toàn bộ quote từ DB
    }

    @Override
    public Quote applyPromotions(Long quoteId, List<Long> promotionIds) {
        return null;
    }

    @Override
    @Transactional
    public Payment makeCashPayment(Long orderId, BigDecimal amount) {
        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Payment p = new Payment();
        p.setOrder(order);                             // <-- khong dung setOrderId
        p.setType(PaymentType.CASH);                   // <-- enum, khong dung String
        p.setAmount(amount);
        // p.setMethod("cash"); // neu muon luu phuong thuc
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
        // p.setMethod("installment");
        return paymentRepo.save(p);
    }

}

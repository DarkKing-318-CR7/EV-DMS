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

        // cap nhat quote
        quote.setStatus("APPROVED");
        quoteRepo.save(quote);

        // tao order tu quote
        OrderHdr order = new OrderHdr();
        order.setQuoteId(quote.getId());
        order.setStatus(OrderStatus.NEW);              // <-- enum, khong dung String
        order.setTotalAmount(quote.getTotalAmount());
        OrderHdr savedOrder = orderRepo.save(order);

        // chuyen QuoteItem -> OrderItem
        if (quote.getItems() != null) {
            for (QuoteItem qi : quote.getItems()) {
                OrderItem oi = new OrderItem();
                oi.setOrder(savedOrder);
                oi.setTrimId(qi.getVehicleId());       // <-- khop field trong OrderItem
                oi.setQty(qi.getQuantity());           // <-- khop field trong OrderItem
                oi.setUnitPrice(qi.getUnitPrice());
                orderItemRepo.save(oi);
            }
        }

        // TODO: hook InventoryService.allocateForOrder(savedOrder.getId());

        return savedOrder;
    }

    @Override
    public Quote submitQuote(Long quoteId) {
        return null;
    }

    @Override
    public Quote rejectQuote(Long quoteId, String comment) {
        return null;
    }

    @Override
    public List<Quote> findPending() {
        return List.of();
    }

    @Override
    public List<Quote> findAll() {
        return List.of();
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

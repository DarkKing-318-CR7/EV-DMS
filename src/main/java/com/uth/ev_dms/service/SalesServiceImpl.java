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
    private final QuoteItemRepo quoteItemRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final PaymentRepo paymentRepo;

    public SalesServiceImpl(QuoteRepo quoteRepo,
                            QuoteItemRepo quoteItemRepo,
                            OrderRepo orderRepo,
                            OrderItemRepo orderItemRepo,
                            PaymentRepo paymentRepo) {
        this.quoteRepo = quoteRepo;
        this.quoteItemRepo = quoteItemRepo;
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

        // Tạo danh sách item
        List<QuoteItem> items = new ArrayList<>();
        if (dto.getItems() != null) {
            for (CreateQuoteItemDTO it : dto.getItems()) {
                QuoteItem qi = new QuoteItem();
                qi.setVehicleId(it.getVehicleId());
                qi.setQuantity(it.getQuantity());
                qi.setUnitPrice(it.getUnitPrice());
                qi.setQuote(q); // Gắn quote vào từng item (1-nhiều)
                items.add(qi);
            }
        }

        q.setItems(items); // Gắn items vào quote (nhiều-1)
        Quote saved = quoteRepo.save(q);

        return saved;
    }




    @Override
    @Transactional
    public OrderHdr approveQuote(Long quoteId) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));

        quote.setStatus("APPROVED");
        quoteRepo.save(quote);

        OrderHdr order = new OrderHdr();
        order.setQuoteId(quote.getId());
        order.setStatus("PENDING");
        order.setTotalAmount(quote.getTotalAmount());
        OrderHdr savedOrder = orderRepo.save(order);

        // chuyển các QuoteItem -> OrderItem
        for (QuoteItem qi : quote.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setVehicleId(qi.getVehicleId());
            oi.setQuantity(qi.getQuantity());
            oi.setUnitPrice(qi.getUnitPrice());
            orderItemRepo.save(oi);
        }

        // TODO: trừ kho ở InventoryService (module A) khi nhóm A expose API/service

        return savedOrder;
    }

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
        // Tạm để giống cash (nhóm E có thể mở rộng: tạo InstallmentPlan)
        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setPaymentType("installment");
        p.setAmount(amount);
        return paymentRepo.save(p);
    }
}

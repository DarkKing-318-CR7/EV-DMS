package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.service.SalesService;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import com.uth.ev_dms.service.dto.PaymentRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    // POST /api/v1/sales/quotes
    @PostMapping("/quotes")
    public ResponseEntity<Quote> createQuote(@RequestBody CreateQuoteDTO dto) {
        Quote quote = salesService.createQuote(dto);
        return ResponseEntity.status(201).body(quote);
    }


    // PATCH /api/v1/sales/quotes/{id}/approve
//    @PatchMapping("/quotes/{id}/approve")
//    public OrderHdr approveQuote(@PathVariable Long id) {
//        return salesService.approveQuote(id);
//    }

    // POST /api/v1/sales/orders/{id}/payments/cash
    @PostMapping("/orders/{id}/payments/cash")
    public Payment payCash(@PathVariable Long id, @RequestBody PaymentRequestDTO request) {
        return salesService.makeCashPayment(id, request.getAmount());
    }

    // POST /api/v1/sales/orders/{id}/payments/installment
    @PostMapping("/orders/{id}/payments/installment")
    public Payment payInstallment(@PathVariable Long id, @RequestBody PaymentRequestDTO request) {
        return salesService.makeInstallmentPayment(id, request.getAmount());
    }

    @PatchMapping("/quotes/{id}/approve")
    public ResponseEntity<OrderHdr> approveQuote(@PathVariable Long id) {
        OrderHdr order = salesService.approveQuote(id);
        return ResponseEntity.ok(order);
    }




}

package com.ev.dms.web.controller;

import com.ev.dms.domain.sales.OrderHdr;
import com.ev.dms.domain.sales.Payment;
import com.ev.dms.domain.sales.Quote;
import com.ev.dms.application.sales.SalesService;
import com.ev.dms.application.sales.dto.CreateQuoteDTO;
import com.ev.dms.application.sales.dto.PaymentRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    // POST /api/v1/sales/quotes
    @PostMapping("/quotes")
    public Quote createQuote(@RequestBody CreateQuoteDTO dto) {
        return salesService.createQuote(dto);
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

    @PostMapping("/quotes/{id}/approve")
    public ResponseEntity<OrderHdr> approveQuote(@PathVariable Long id) {
        OrderHdr order = salesService.approveQuote(id);
        return ResponseEntity.ok(order);
    }

}

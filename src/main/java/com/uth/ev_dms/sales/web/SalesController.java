package com.uth.ev_dms.sales.web;

import com.uth.ev_dms.sales.domain.OrderHdr;
import com.uth.ev_dms.sales.domain.Payment;
import com.uth.ev_dms.sales.domain.Quote;
import com.uth.ev_dms.sales.service.SalesService;
import com.uth.ev_dms.sales.service.dto.CreateQuoteDTO;
import com.uth.ev_dms.sales.service.dto.PaymentRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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

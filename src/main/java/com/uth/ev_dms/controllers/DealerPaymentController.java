package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/dealer/orders")
@RequiredArgsConstructor
public class DealerPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/payments")
    public String addPayment(@PathVariable Long orderId,
                             @RequestParam BigDecimal amount,
                             @RequestParam String method,
                             @RequestParam(required = false) String note) {
        paymentService.addPayment(orderId, amount, method, note);
        return "redirect:/dealer/orders/detail/" + orderId;
    }

    @PostMapping("/{orderId}/installment")
    public String createInstallment(@PathVariable Long orderId,
                                    @RequestParam int months,
                                    @RequestParam BigDecimal interestRate) {
        paymentService.createInstallment(orderId, months, interestRate);
        return "redirect:/dealer/orders/detail/" + orderId;
    }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/dealer/payments") // ⬅️ tách prefix để không đụng /dealer/orders
@RequiredArgsConstructor
public class DealerPaymentController {

    private final PaymentService paymentService;
    private final OrderRepo orderRepo;

    // POST /dealer/payments/orders/{orderId}/payments
    @PostMapping("/orders/{orderId}/payments")
    public String addPayment(@PathVariable Long orderId,
                             @RequestParam BigDecimal amount,
                             @RequestParam String method,
                             @RequestParam(required = false) String refNo,
                             RedirectAttributes ra) {
        try {
            paymentService.addPayment(orderId, amount, method, refNo);
            ra.addFlashAttribute("ok", "Đã ghi nhận thanh toán " + method + " số tiền " + amount);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dealer/orders/" + orderId;
    }

    // POST /dealer/payments/orders/{orderId}/installment
    @PostMapping("/orders/{orderId}/installment")
    public String createInstallment(@PathVariable Long orderId,
                                    @RequestParam("months") int months,
                                    @RequestParam("downPayment") BigDecimal downPayment,
                                    RedirectAttributes ra) {
        try {
            OrderHdr order = orderRepo.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("ORDER_NOT_FOUND"));

            // Chặn các trạng thái không hợp lệ
            if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
                ra.addFlashAttribute("error", "Không thể tạo trả góp cho đơn ở trạng thái " + order.getStatus());
                return "redirect:/dealer/orders/" + orderId;
            }
            // Chỉ cho NEW hoặc PENDING_ALLOC (theo policy hiện tại)
            if (!(order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PENDING_ALLOC)) {
                ra.addFlashAttribute("error", "Trạng thái hiện tại không cho phép tạo trả góp.");
                return "redirect:/dealer/orders/" + orderId;
            }

            paymentService.createInstallment(orderId, months, downPayment);
            ra.addFlashAttribute("ok", "Đã tạo trả góp " + months + " tháng, trả trước " + downPayment);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dealer/orders/" + orderId;
    }
}

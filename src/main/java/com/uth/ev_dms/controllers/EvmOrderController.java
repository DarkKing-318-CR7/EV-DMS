package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.service.OrderService;
import com.uth.ev_dms.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/evm/orders")
public class EvmOrderController {

    private final OrderRepo orderRepo;
    private final OrderService orderService;     // dùng cho chi tiết/allocate
    private final PaymentService paymentService; // tính tiền đã thanh toán

    @GetMapping
    public String listAll(Model model) {
        List<OrderHdr> list = orderRepo.findAll(); // có thể thêm filter theo nhu cầu
        model.addAttribute("orders", list);
        return "evm/orders/list";
    }

    @GetMapping("/pending")
    public String listPending(Model model) {
        List<OrderHdr> list = orderRepo.findByStatusOrderByIdDesc(OrderStatus.PENDING_ALLOC);
        model.addAttribute("orders", list);
        return "evm/orders/pending";
    }

    @GetMapping("/{id}") // ✅ đúng: /evm/orders/{id}
    public String detail(@PathVariable Long id, Model model) {
        OrderHdr order = orderService.findById(id);
        List<OrderItem> items = orderService.findItems(id);
        List<Payment> payments = paymentService.findByOrderId(id);

        BigDecimal paid = payments.stream()
                .map(Payment::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal balance = total.subtract(paid);

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("payments", payments);
        model.addAttribute("amountPaid", paid);   // biến rời cho Thymeleaf
        model.addAttribute("balance", balance);   // biến rời cho Thymeleaf
        // model.addAttribute("allocLogs", allocLogsService.findByOrderId(id)); // nếu có

        return "evm/orders/detail";
    }
    @PostMapping("/{id}/approve-allocate")
    public String approveAllocate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderService.allocate(id);
            ra.addFlashAttribute("ok", "Đã duyệt & phân bổ thành công đơn #" + id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi duyệt: " + e.getMessage());
        }
        return "redirect:/evm/orders/" + id;
    }
    @PostMapping("/{id}/deallocate")
    public String deallocate(@PathVariable Long id, RedirectAttributes ra) {
        orderService.deallocateByEvm(id, null, "manual");
        ra.addFlashAttribute("ok", "Đã thu hồi phân bổ đơn #" + id);
        return "redirect:/evm/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        orderService.cancelByEvm(id, null, "manual");
        ra.addFlashAttribute("ok", "Đã hủy đơn #" + id);
        return "redirect:/evm/orders/" + id;
    }




}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.service.OrderService;
import com.uth.ev_dms.service.PaymentService;
import com.uth.ev_dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dealer/orders")
@RequiredArgsConstructor
public class DealerOrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderRepo orderRepo;
    private final PaymentService paymentService;

    @GetMapping("/my")
    public String myOrders(Model model, Principal principal) {
        String username = principal.getName();
        Long staffId  = userService.findIdByUsername(username);
        Long dealerId = userService.findDealerIdByUsername(username);

        System.out.println(">>> DEBUG /dealer/orders/my: username=" + username +
                ", staffId=" + staffId + ", dealerId=" + dealerId);

        var orders = (dealerId == null)
                ? orderRepo.findBySalesStaffIdOrderByIdDesc(staffId)
                : orderRepo.findBySalesStaffIdAndDealerIdOrderByIdDesc(staffId, dealerId);

        model.addAttribute("orders", orders);
        return "dealer/orders/my-list";
    }


    @GetMapping
    public String listAll(Model model, Principal principal) {
        Long dealerId = userService.findDealerIdByUsername(principal.getName());
        List<OrderHdr> orders = orderService.findAllForDealer(dealerId);
        model.addAttribute("orders", orders);
        return "dealer/orders/list";
    }

    @PostMapping("/{orderId}/submit")
    public String submit(@PathVariable("orderId") Long orderId) {
        orderService.submitForAllocation(orderId);
        return "redirect:/dealer/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var order = orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);
        model.addAttribute("items", order.getItems());
        model.addAttribute("payments", order.getPayments());

        BigDecimal total = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        BigDecimal paid  = order.getPaidAmount()  == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal bal   = order.getBalanceAmount() == null ? total.subtract(paid) : order.getBalanceAmount();

        model.addAttribute("totalAmountSafe", total);
        model.addAttribute("amountPaid", paid);
        model.addAttribute("balance", bal);

        boolean isNew = order.getStatus() == OrderStatus.NEW;
        model.addAttribute("isNew", isNew);

        // ===== Trả góp: chỉ NEW/PENDING_ALLOC và chưa có kế hoạch trước đó
        boolean hasInstallment = paymentService.hasInstallment(id);
        boolean canInstallment =
                (order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PENDING_ALLOC)
                        && !hasInstallment;
        model.addAttribute("hasInstallment", hasInstallment);
        model.addAttribute("canInstallment", canInstallment);

        return "dealer/orders/detail";
    }

    @PostMapping("/{id}/allocate")
    public String allocate(@PathVariable Long id, RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(id).orElseThrow(() -> new RuntimeException("ORDER_NOT_FOUND"));
        if (o.getStatus() == OrderStatus.NEW) {
            o.setStatus(OrderStatus.PENDING_ALLOC);
            orderRepo.save(o);
            ra.addFlashAttribute("ok", "Đã gửi yêu cầu cấp xe.");
        } else {
            ra.addFlashAttribute("error", "Trạng thái hiện tại không cho phép xin cấp xe.");
        }
        return "redirect:/dealer/orders/" + id;
    }

    @PostMapping("/{orderId}/pay-cash")
    public String payCash(@PathVariable Long orderId,
                          @RequestParam BigDecimal amount,
                          @RequestParam(required = false) String refNo,
                          RedirectAttributes ra) {
        try {
            paymentService.addPayment(orderId, amount, "CASH", refNo);
            ra.addFlashAttribute("ok", "Đã ghi nhận thanh toán tiền mặt.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dealer/orders/" + orderId;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra, Principal p) {
        var dealerId = userService.findDealerIdByUsername(p.getName());
        orderService.cancelByDealer(id, dealerId, null);
        ra.addFlashAttribute("ok", "Đã hủy đơn #" + id);
        return "redirect:/dealer/orders";
    }

    @PostMapping("/{id}/request-allocate")
    public String requestAllocate(@PathVariable Long id, Principal p, RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(id).orElseThrow();
        if (o.getStatus() != OrderStatus.NEW) {
            ra.addFlashAttribute("err", "Chỉ đơn NEW mới được xin cấp.");
            return "redirect:/dealer/orders/" + id;
        }
        o.setStatus(OrderStatus.PENDING_ALLOC);
        Long uid = userService.getUserId(p);
        o.setCreatedBy(uid);
        orderRepo.save(o);
        ra.addFlashAttribute("ok", "Đã gửi yêu cầu cấp xe (PENDING_ALLOC).");
        return "redirect:/dealer/orders/" + id;
    }

    // ====== Tạo kế hoạch trả góp
    @PostMapping("/{orderId}/installment")
    public String createInstallment(@PathVariable Long orderId,
                                    @RequestParam("months") int months,
                                    @RequestParam("downPayment") BigDecimal downPayment,
                                    RedirectAttributes ra) {
        try {
            paymentService.createInstallment(orderId, months, downPayment);
            ra.addFlashAttribute("ok", "Đã tạo trả góp " + months + " tháng, trả trước " + downPayment);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dealer/orders/" + orderId;
    }

}

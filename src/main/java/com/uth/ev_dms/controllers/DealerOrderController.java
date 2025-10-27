package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.service.OrderService;
import com.uth.ev_dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dealer/orders")
@RequiredArgsConstructor
public class DealerOrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderRepo orderRepo;   //

    // GET /dealer/orders/my
    @GetMapping("/my")
    public String myOrders(Model model, Principal principal) {
        Long staffId = userService.findIdByUsername(principal.getName());
        var orders = orderService.findMine(staffId);
        model.addAttribute("orders", orders);
        return "dealer/orders/my-list";
    }

    // GET /dealer/orders
    @GetMapping
    public String listAll(Model model, Principal principal) {
        Long dealerId = userService.findDealerIdByUsername(principal.getName());
        List<OrderHdr> orders = orderService.findAllForDealer(dealerId);
        model.addAttribute("orders", orders);
        return "dealer/orders/list";
    }

    // POST /dealer/orders/{id}/submit
    @PostMapping("/{orderId}/submit")
    public String submit(@PathVariable("orderId") Long orderId) {
        orderService.submitForAllocation(orderId);
        return "redirect:/dealer/orders";
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        model.addAttribute("order", order);
        model.addAttribute("items", order.getItems());
        model.addAttribute("payments", order.getPayments());
        return "dealer/orders/detail"; // ✅ đường dẫn tới template
    }
    @PostMapping("/{id}/allocate")
    public String allocate(@PathVariable Long id, RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("ORDER_NOT_FOUND"));
        if (o.getStatus() == OrderStatus.NEW) {
            o.setStatus(OrderStatus.PENDING_ALLOC);
            orderRepo.save(o);
            ra.addFlashAttribute("ok", "Đã gửi yêu cầu cấp xe.");
        } else {
            ra.addFlashAttribute("error", "Trạng thái hiện tại không cho phép xin cấp xe.");
        }
        return "redirect:/dealer/orders/" + id;
    }

    // huỷ đơn
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("ORDER_NOT_FOUND"));
        if (o.getStatus() == OrderStatus.NEW) {
            o.setStatus(OrderStatus.CANCELLED);
            orderRepo.save(o);
            ra.addFlashAttribute("ok", "Đã huỷ đơn.");
        } else {
            ra.addFlashAttribute("error", "Chỉ huỷ được đơn trạng thái NEW.");
        }
        return "redirect:/dealer/orders/" + id;
    }

    // thanh toán tiền mặt demo (tuỳ bạn có entity Payment hay không)
    @PostMapping("/{id}/pay-cash")
    public String payCash(@PathVariable Long id, RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("ORDER_NOT_FOUND"));
        // TODO: thêm Payment, cập nhật tổng tiền đã trả...
        ra.addFlashAttribute("ok", "Đã ghi nhận thanh toán (demo).");
        return "redirect:/dealer/orders/" + id;
    }

}

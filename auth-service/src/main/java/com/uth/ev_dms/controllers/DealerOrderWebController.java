package com.uth.ev_dms.controllers;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.dto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dealer/orders")
@RequiredArgsConstructor
public class DealerOrderWebController {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final PaymentRepo paymentRepo;
    private final InstallmentPlanRepo installmentPlanRepo;
    private final TrimRepo trimRepo;
    private final UserRepo userRepo;

    private User getCurrentUser(Authentication auth) {
        if (auth == null) return null;
        return userRepo.findByUsername(auth.getName()).orElse(null);
    }

    private boolean isManager(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_DEALER_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ================= MY ORDERS =================
    @GetMapping("/my")
    public String myOrders(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        Long staffId = user != null ? user.getId() : 1L;
        Long dealerId = (user != null && user.getDealer() != null) ? user.getDealer().getId() : 1L;

        List<OrderHdr> orders;
        if (isManager(auth)) {
            orders = orderRepo.findAllForDealer(dealerId);
            if (orders.isEmpty()) {
                orders = orderRepo.findAll();
            }
        } else {
            orders = orderRepo.findBySalesStaffIdOrderByIdDesc(staffId);
            if (orders.isEmpty()) {
                orders = orderRepo.findMyOrdersForStaff(dealerId, staffId);
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("active", "orders");
        model.addAttribute("pageTitle", "Đơn hàng của tôi");
        return "dealer/orders/my-list";
    }

    // ================= ALL ORDERS (MANAGER) =================
    @GetMapping
    public String listAll(Model model, Authentication auth) {
        if (!isManager(auth)) {
            return "redirect:/dealer/orders/my";
        }

        User user = getCurrentUser(auth);
        Long dealerId = (user != null && user.getDealer() != null) ? user.getDealer().getId() : 1L;

        List<OrderHdr> orders = orderRepo.findAllForDealer(dealerId);
        if (orders.isEmpty()) {
            orders = orderRepo.findAll();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("active", "orders");
        model.addAttribute("pageTitle", "Tất cả đơn hàng");
        return "dealer/orders/list";
    }

    // ================= DETAIL PAGE =================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OrderHdr order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng #" + id));

        var itemDtos = order.getItems().stream().map(it -> {
            OrderItemDto dto = new OrderItemDto();
            dto.setId(it.getId());
            dto.setTrimId(it.getTrimId());

            Trim trim = it.getTrimId() != null ? trimRepo.findById(it.getTrimId()).orElse(null) : null;
            dto.setTrimName(trim != null ? trim.getTrimName() : "N/A");

            Vehicle vehicle = (trim != null ? trim.getVehicle() : null);
            dto.setVehicleName(vehicle != null ? vehicle.getModelName() : "Unknown Model");

            dto.setUnitPrice(it.getUnitPrice());
            dto.setQty(it.getQty());
            dto.setDiscountAmount(it.getDiscountAmount());
            return dto;
        }).toList();

        model.addAttribute("order", order);
        model.addAttribute("items", itemDtos);
        model.addAttribute("payments", order.getPayments());

        BigDecimal total = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        BigDecimal paid = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal bal = order.getBalanceAmount() == null ? total.subtract(paid) : order.getBalanceAmount();

        model.addAttribute("totalAmountSafe", total);
        model.addAttribute("amountPaid", paid);
        model.addAttribute("balance", bal);

        boolean hasInstallment = installmentPlanRepo.existsByOrderId(id);
        boolean canInstallment = (order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PENDING_ALLOC) && !hasInstallment;

        model.addAttribute("hasInstallment", hasInstallment);
        model.addAttribute("canInstallment", canInstallment);
        model.addAttribute("active", "orders");
        model.addAttribute("pageTitle", "Chi tiết đơn #" + id);

        return "dealer/orders/detail";
    }

    // ================= ALLOCATE =================
    @PostMapping("/{id}/allocate")
    @Transactional
    public String allocate(@PathVariable Long id, RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        o.setStatus(OrderStatus.ALLOCATED);
        if (o.getAllocatedAt() == null) o.setAllocatedAt(LocalDateTime.now());
        orderRepo.save(o);

        ra.addFlashAttribute("ok", "✔ Đã cấp xe thành công.");
        return "redirect:/dealer/orders/" + id;
    }

    // ================= REQUEST ALLOCATE =================
    @PostMapping("/{id}/request-allocate")
    @Transactional
    public String requestAllocate(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        o.setStatus(OrderStatus.PENDING_ALLOC);
        if (o.getSubmittedAt() == null) o.setSubmittedAt(LocalDateTime.now());

        User user = getCurrentUser(auth);
        if (user != null) {
            o.setCreatedBy(user.getId());
        }
        orderRepo.save(o);

        ra.addFlashAttribute("ok", "Đã gửi yêu cầu cấp xe tới Hãng.");
        return "redirect:/dealer/orders/" + id;
    }

    // ================= PAY CASH =================
    @PostMapping("/{orderId}/pay-cash")
    @Transactional
    public String payCash(@PathVariable Long orderId,
                          @RequestParam BigDecimal amount,
                          @RequestParam(required = false) String refNo,
                          RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        try {
            Payment p = new Payment();
            p.setOrder(o);
            p.setAmount(amount);
            p.setMethod("CASH");
            p.setType(PaymentType.CASH);
            p.setRefNo(refNo);
            p.setPaidAt(LocalDateTime.now());
            paymentRepo.save(p);

            BigDecimal currentPaid = o.getPaidAmount() != null ? o.getPaidAmount() : BigDecimal.ZERO;
            o.setPaidAmount(currentPaid.add(amount));
            orderRepo.save(o);

            ra.addFlashAttribute("ok", "Đã ghi nhận thanh toán tiền mặt thành công.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Lỗi ghi nhận thanh toán: " + ex.getMessage());
        }

        return "redirect:/dealer/orders/" + orderId;
    }

    // ================= CANCEL =================
    @PostMapping("/{id}/cancel")
    @Transactional
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        OrderHdr order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        ra.addFlashAttribute("ok", "Đã hủy đơn #" + id);

        return "redirect:/dealer/orders";
    }

    // ================= INSTALLMENT =================
    @PostMapping("/{orderId}/installment")
    @Transactional
    public String createInstallment(@PathVariable Long orderId,
                                    @RequestParam("months") int months,
                                    @RequestParam("downPayment") BigDecimal downPayment,
                                    RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        try {
            InstallmentPlan plan = new InstallmentPlan();
            plan.setOrder(o);
            plan.setTenorMonths(months);
            plan.setDownPayment(downPayment);
            plan.setApprovedAt(LocalDateTime.now());
            installmentPlanRepo.save(plan);

            ra.addFlashAttribute("ok", "Đã lập kế hoạch trả góp thành công.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Lỗi lập trả góp: " + ex.getMessage());
        }

        return "redirect:/dealer/orders/" + orderId;
    }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.repo.TrimRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.service.OrderService;
import com.uth.ev_dms.service.PaymentService;
import com.uth.ev_dms.service.UserService;
import com.uth.ev_dms.service.dto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/dealer/orders")
@RequiredArgsConstructor
public class DealerOrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderRepo orderRepo;
    private final PaymentService paymentService;
    private final TrimRepo trimRepo;
    private final VehicleRepo vehicleRepo;

    // ================= ROLE HELPERS =================
    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private boolean isManager() { return hasRole("ROLE_DEALER_MANAGER"); }
    private boolean isStaff()   { return hasRole("ROLE_DEALER_STAFF"); }

    private void assertCanAccess(Principal principal, OrderHdr o) {
        String username = principal.getName();
        Long meId = userService.findIdByUsername(username);
        Long myDealer = userService.findDealerIdByUsername(username);

        if (isManager()) {
            if (!Objects.equals(o.getDealerId(), myDealer)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không cùng đại lý");
            }
            return;
        }

        if (isStaff()) {
            boolean mine = Objects.equals(o.getSalesStaffId(), meId)
                    || Objects.equals(o.getCreatedBy(), meId);

            if (!mine) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không phải đơn của bạn");
            }
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    // ================= MY ORDERS =================
    @GetMapping("/my")
    public String myOrders(Model model, Principal principal) {
        String username = principal.getName();
        Long staffId  = userService.findIdByUsername(username);
        Long dealerId = userService.findDealerIdByUsername(username);

        List<OrderHdr> orders = isManager()
                ? orderService.findAllForDealer(dealerId)
                : (dealerId == null
                ? orderRepo.findBySalesStaffIdOrderByIdDesc(staffId)
                : orderRepo.findBySalesStaffIdAndDealerIdOrderByIdDesc(staffId, dealerId));

        model.addAttribute("orders", orders);
        return "dealer/orders/my-list";
    }

    // ================= ALL ORDERS (MANAGER) =================
    @GetMapping
    public String listAll(Model model, Principal principal) {
        if (isStaff()) {
            return "redirect:/dealer/orders/my";
        }

        Long dealerId = userService.findDealerIdByUsername(principal.getName());
        List<OrderHdr> orders = orderService.findAllForDealer(dealerId);

        model.addAttribute("orders", orders);
        return "dealer/orders/list";
    }

    // ================= DETAIL PAGE =================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {

        OrderHdr order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(principal, order);

        List<OrderItem> items = order.getItems();

        // ========= MAP ITEMS → DTO =========
        var itemDtos = items.stream().map(it -> {
            OrderItemDto dto = new OrderItemDto();
            dto.setId(it.getId());
            dto.setTrimId(it.getTrimId());

            // --- Lấy Trim ---
            Trim trim = null;
            if (it.getTrimId() != null) {
                trim = trimRepo.findById(it.getTrimId()).orElse(null);
            }

            dto.setTrimName(trim != null ? trim.getTrimName() : "N/A");

            // --- Lấy Vehicle qua Trim ---
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
        BigDecimal paid  = order.getPaidAmount()  == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal bal   = order.getBalanceAmount() == null ? total.subtract(paid) : order.getBalanceAmount();

        model.addAttribute("totalAmountSafe", total);
        model.addAttribute("amountPaid", paid);
        model.addAttribute("balance", bal);

        model.addAttribute("isNew", order.getStatus() == OrderStatus.NEW);

        boolean hasInstallment = paymentService.hasInstallment(id);
        boolean canInstallment =
                (order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PENDING_ALLOC)
                        && !hasInstallment;

        model.addAttribute("hasInstallment", hasInstallment);
        model.addAttribute("canInstallment", canInstallment);

        return "dealer/orders/detail";
    }

    // ================= ACTIONS =================

    @PostMapping("/{id}/allocate")
    public String allocate(@PathVariable Long id, RedirectAttributes ra, Principal principal) {

        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(principal, o);

        if (o.getStatus() == OrderStatus.NEW) {
            o.setStatus(OrderStatus.PENDING_ALLOC);

            if (o.getSubmittedAt() == null) {
                o.setSubmittedAt(LocalDateTime.now());
            }

            orderRepo.save(o);
            ra.addFlashAttribute("ok", "Đã gửi yêu cầu cấp xe.");
        } else {
            ra.addFlashAttribute("error", "Trạng thái hiện tại không cho phép xin cấp xe.");
        }

        return "redirect:/dealer/orders/" + id;
    }

    @PostMapping("/{orderId}/pay-cash")
    public String payCash(
            @PathVariable Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String refNo,
            RedirectAttributes ra,
            Principal principal) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(principal, o);

        try {
            paymentService.addPayment(orderId, amount, "CASH", refNo);
            ra.addFlashAttribute("ok", "Đã ghi nhận thanh toán tiền mặt.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/dealer/orders/" + orderId;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra, Principal principal) {

        OrderHdr order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(principal, order);

        Long dealerId = userService.findDealerIdByUsername(principal.getName());

        orderService.cancelByDealer(id, dealerId, null);
        ra.addFlashAttribute("ok", "Đã hủy đơn #" + id);

        return "redirect:/dealer/orders";
    }

    @PostMapping("/{id}/request-allocate")
    public String requestAllocate(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes ra) {

        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(principal, o);

        if (o.getStatus() != OrderStatus.NEW) {
            ra.addFlashAttribute("err", "Chỉ đơn NEW mới được xin cấp.");
            return "redirect:/dealer/orders/" + id;
        }

        o.setStatus(OrderStatus.PENDING_ALLOC);

        if (o.getSubmittedAt() == null) {
            o.setSubmittedAt(LocalDateTime.now());
        }

        Long uid = userService.getUserId(principal);
        o.setCreatedBy(uid);

        orderRepo.save(o);

        ra.addFlashAttribute("ok", "Đã gửi yêu cầu cấp xe (PENDING_ALLOC).");

        return "redirect:/dealer/orders/" + id;
    }

    @PostMapping("/{orderId}/installment")
    public String createInstallment(
            @PathVariable Long orderId,
            @RequestParam("months") int months,
            @RequestParam("downPayment") BigDecimal downPayment,
            RedirectAttributes ra,
            Principal principal) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(principal, o);

        try {
            paymentService.createInstallment(orderId, months, downPayment);
            ra.addFlashAttribute("ok", "Đã tạo trả góp " + months + " tháng, trả trước " + downPayment);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/dealer/orders/" + orderId;
    }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.repo.InventoryRepo;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.repo.TrimRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.service.OrderService;
import com.uth.ev_dms.service.PaymentService;
import com.uth.ev_dms.service.dto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/dealer/orders")
@RequiredArgsConstructor
public class DealerOrderController {

    private final OrderService orderService;
    private final OrderRepo orderRepo;
    private final PaymentService paymentService;
    private final TrimRepo trimRepo;
    private final VehicleRepo vehicleRepo;
    private final InventoryRepo inventoryRepo;

    // ================= HEADER HELPERS =================
    private Long getUserId(HttpServletRequest request) {
        return Long.valueOf(request.getHeader("X-User-Id"));
    }

    private Long getDealerId(HttpServletRequest request) {
        return Long.valueOf(request.getHeader("X-Dealer-Id"));
    }

    private String getRole(HttpServletRequest request) {
        return request.getHeader("X-Role");
    }

    private boolean isManager(HttpServletRequest request) {
        return "ROLE_DEALER_MANAGER".equals(getRole(request));
    }

    private boolean isStaff(HttpServletRequest request) {
        return "ROLE_DEALER_STAFF".equals(getRole(request));
    }

    // ================= ACCESS CONTROL =================
    private void assertCanAccess(HttpServletRequest request, OrderHdr o) {
        Long meId = getUserId(request);
        Long myDealer = getDealerId(request);

        if (isManager(request)) {
            if (!Objects.equals(o.getDealerId(), myDealer)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không cùng đại lý");
            }
            return;
        }

        if (isStaff(request)) {
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
    public String myOrders(Model model, HttpServletRequest request) {

        Long staffId = getUserId(request);
        Long dealerId = getDealerId(request);

        List<OrderHdr> orders = isManager(request)
                ? orderService.findAllForDealer(dealerId)
                : orderRepo.findBySalesStaffIdAndDealerIdOrderByIdDesc(staffId, dealerId);

        model.addAttribute("orders", orders);
        return "dealer/orders/my-list";
    }

    // ================= ALL ORDERS (MANAGER) =================
    @GetMapping
    public String listAll(Model model, HttpServletRequest request) {

        if (isStaff(request)) {
            return "redirect:/dealer/orders/my";
        }

        Long dealerId = getDealerId(request);
        List<OrderHdr> orders = orderService.findAllForDealer(dealerId);

        model.addAttribute("orders", orders);
        return "dealer/orders/list";
    }

    // ================= DETAIL PAGE =================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request) {

        OrderHdr order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(request, order);

        // items → dto
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
        BigDecimal paid  = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal bal   = order.getBalanceAmount() == null ? total.subtract(paid) : order.getBalanceAmount();

        model.addAttribute("totalAmountSafe", total);
        model.addAttribute("amountPaid", paid);
        model.addAttribute("balance", bal);

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
    @Transactional
    public String allocate(@PathVariable Long id, RedirectAttributes ra, HttpServletRequest request) {

        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(request, o);

        if (o.getStatus() != OrderStatus.NEW) {
            ra.addFlashAttribute("error", "Trạng thái hiện tại không cho phép xin cấp xe.");
            return "redirect:/dealer/orders/" + id;
        }

        Long dealerId = o.getDealerId();
        if (dealerId == null) {
            ra.addFlashAttribute("error", "Dealer ID không hợp lệ.");
            return "redirect:/dealer/orders/" + id;
        }

        for (OrderItem it : o.getItems()) {
            int ok = inventoryRepo.reduceInventory(dealerId, it.getTrimId(), it.getQty());
            if (ok == 0) {
                ra.addFlashAttribute("error", "Kho không đủ xe cho trim " + it.getTrimId());
                return "redirect:/dealer/orders/" + id;
            }
        }

        o.setStatus(OrderStatus.PENDING_ALLOC);
        if (o.getSubmittedAt() == null) o.setSubmittedAt(LocalDateTime.now());
        orderRepo.save(o);

        ra.addFlashAttribute("ok", "✔ Đã xin cấp xe.");
        return "redirect:/dealer/orders/" + id;
    }

    @PostMapping("/{orderId}/pay-cash")
    public String payCash(
            @PathVariable Long orderId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String refNo,
            RedirectAttributes ra,
            HttpServletRequest request) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(request, o);

        try {
            paymentService.addPayment(orderId, amount, "CASH", refNo);
            ra.addFlashAttribute("ok", "Đã ghi nhận thanh toán tiền mặt.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/dealer/orders/" + orderId;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra, HttpServletRequest request) {

        OrderHdr order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(request, order);

        Long dealerId = getDealerId(request);

        orderService.cancelByDealer(id, dealerId, null);
        ra.addFlashAttribute("ok", "Đã hủy đơn #" + id);

        return "redirect:/dealer/orders";
    }

    @PostMapping("/{id}/request-allocate")
    public String requestAllocate(
            @PathVariable Long id,
            HttpServletRequest request,
            RedirectAttributes ra) {

        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(request, o);

        if (o.getStatus() != OrderStatus.NEW) {
            ra.addFlashAttribute("err", "Chỉ đơn NEW mới được xin cấp.");
            return "redirect:/dealer/orders/" + id;
        }

        o.setStatus(OrderStatus.PENDING_ALLOC);

        if (o.getSubmittedAt() == null) o.setSubmittedAt(LocalDateTime.now());

        Long uid = getUserId(request);
        o.setCreatedBy(uid);

        orderRepo.save(o);

        ra.addFlashAttribute("ok", "Đã gửi yêu cầu cấp xe.");
        return "redirect:/dealer/orders/" + id;
    }

    @PostMapping("/{orderId}/installment")
    public String createInstallment(
            @PathVariable Long orderId,
            @RequestParam("months") int months,
            @RequestParam("downPayment") BigDecimal downPayment,
            RedirectAttributes ra,
            HttpServletRequest request) {

        OrderHdr o = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanAccess(request, o);

        try {
            paymentService.createInstallment(orderId, months, downPayment);
            ra.addFlashAttribute("ok", "Đã tạo trả góp.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/dealer/orders/" + orderId;
    }
}

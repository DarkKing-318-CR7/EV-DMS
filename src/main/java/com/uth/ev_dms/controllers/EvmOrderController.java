package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.repo.TrimRepo;
import com.uth.ev_dms.repo.VehicleRepo;

import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.service.OrderService;
import com.uth.ev_dms.service.PaymentService;
import com.uth.ev_dms.service.dto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
@RequestMapping("/evm/orders")
public class EvmOrderController {

    private final OrderRepo orderRepo;
    private final OrderService orderService;
    private final PaymentService paymentService;

    private final TrimRepo trimRepo;
    private final VehicleRepo vehicleRepo;

    // ========================= LIST + FILTER =========================
    @GetMapping
    public String listAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {
        List<OrderHdr> all = orderRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        Stream<OrderHdr> stream = all.stream();

        if (q != null && !q.isBlank()) {
            String kw = q.trim().toLowerCase();
            stream = stream.filter(o ->
                    String.valueOf(o.getId()).toLowerCase().contains(kw) ||
                            (o.getDealerId() != null && String.valueOf(o.getDealerId()).toLowerCase().contains(kw)) ||
                            (o.getCustomerId() != null && String.valueOf(o.getCustomerId()).toLowerCase().contains(kw)) ||
                            (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(kw))
            );
        }

        if (status != null && !status.isBlank()) {
            try {
                OrderStatus st = OrderStatus.valueOf(status.trim());
                stream = stream.filter(o -> o.getStatus() == st);
            } catch (Exception ignored) {}
        }

        if (from != null) {
            stream = stream.filter(o -> o.getCreatedAt() != null &&
                    !o.getCreatedAt().toLocalDate().isBefore(from));
        }
        if (to != null) {
            stream = stream.filter(o -> o.getCreatedAt() != null &&
                    !o.getCreatedAt().toLocalDate().isAfter(to));
        }

        List<OrderHdr> filtered = stream.toList();

        Map<String, Long> stat = new HashMap<>();
        stat.put("tongDon", (long) filtered.size());
        stat.put("choPhanBo", all.stream().filter(o -> o.getStatus() == OrderStatus.PENDING_ALLOC).count());
        stat.put("daPhanBo", all.stream().filter(o -> o.getStatus() == OrderStatus.ALLOCATED).count());

        model.addAttribute("orders", filtered);
        model.addAttribute("stat", stat);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "evm/orders/list";
    }

    @GetMapping("/pending")
    public String listPending(RedirectAttributes ra) {
        ra.addAttribute("status", OrderStatus.PENDING_ALLOC.name());
        return "redirect:/evm/orders";
    }

    // ========================= DETAIL =========================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        OrderHdr order = orderService.findById(id);
        List<OrderItem> items = orderService.findItems(id);
        List<Payment> payments = paymentService.findByOrderId(id);

        // ========== MAP ORDER ITEM → DTO ==========
        List<OrderItemDto> itemDtos = items.stream().map(it -> {

            OrderItemDto dto = new OrderItemDto();
            dto.setId(it.getId());
            dto.setTrimId(it.getTrimId());

            // ---- JOIN TRIM ----
            Trim trim = null;
            if (it.getTrimId() != null)
                trim = trimRepo.findById(it.getTrimId()).orElse(null);

            dto.setTrimName(trim != null ? trim.getTrimName() : "N/A");

            // ---- JOIN VEHICLE ----
            Vehicle vehicle = (trim != null ? trim.getVehicle() : null);
            dto.setVehicleName(vehicle != null ? vehicle.getModelName() : "Unknown Model");

            dto.setUnitPrice(it.getUnitPrice());
            dto.setQty(it.getQty());
            dto.setDiscountAmount(it.getDiscountAmount());

            return dto;
        }).toList();

        BigDecimal paid = payments.stream()
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal balance = total.subtract(paid);

        model.addAttribute("order", order);
        model.addAttribute("items", itemDtos);
        model.addAttribute("payments", payments);
        model.addAttribute("amountPaid", paid);
        model.addAttribute("balance", balance);

        return "evm/orders/detail";
    }

    // ========================= ACTIONS =========================

    @PostMapping("/{id}/approve-allocate")
    public String approveAllocate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            OrderHdr o = orderService.allocate(id);

            if (o.getAllocatedAt() == null) {
                o.setAllocatedAt(LocalDateTime.now());
                orderRepo.save(o);
            }

            ra.addFlashAttribute("ok", "Đã duyệt & phân bổ thành công đơn #" + id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi duyệt: " + e.getMessage());
        }
        return "redirect:/evm/orders/" + id;
    }


    @PostMapping("/{id}/deallocate")
    public String deallocate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderService.deallocateByEvm(id, null, "manual");
            ra.addFlashAttribute("ok", "Đã thu hồi phân bổ đơn #" + id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thu hồi được: " + e.getMessage());
        }
        return "redirect:/evm/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            orderService.cancelByEvm(id, null, "manual");
            ra.addFlashAttribute("ok", "Đã hủy đơn #" + id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không hủy được: " + e.getMessage());
        }
        return "redirect:/evm/orders/" + id;
    }

    @PostMapping("/{id}/deliver")
    public String deliver(@PathVariable Long id, RedirectAttributes ra) {
        try {
            OrderHdr o = orderService.markDelivered(id);

            if (o.getDeliveredAt() == null) {
                o.setDeliveredAt(LocalDateTime.now());
                orderRepo.save(o);
            }

            ra.addFlashAttribute("ok", "Đơn #" + id + " đã giao.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không giao được: " + e.getMessage());
        }
        return "redirect:/evm/orders/" + id;
    }
}

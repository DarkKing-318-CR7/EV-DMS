package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.dto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Controller
@RequestMapping("/evm")
@RequiredArgsConstructor
public class EvmWebController {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final TrimRepo trimRepo;
    private final VehicleRepo vehicleRepo;
    private final InventoryRepo inventoryRepo;
    private final PromotionRepo promotionRepo;

    // ================= DASHBOARD =================
    @GetMapping({"", "/", "/dashboard", "/home"})
    public String dashboard(Model model) {
        long totalPromotions = promotionRepo.count();
        long activePromotions = promotionRepo.findAll().stream().filter(p -> Boolean.TRUE.equals(p.getActive())).count();

        model.addAttribute("totalPromotions", totalPromotions);
        model.addAttribute("activePromotions", activePromotions);
        model.addAttribute("active", "dashboard");
        model.addAttribute("pageTitle", "EVM Dashboard");
        return "evm/home";
    }

    // ================= ORDERS =================
    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
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
            stream = stream.filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().toLocalDate().isBefore(from));
        }

        if (to != null) {
            stream = stream.filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().toLocalDate().isAfter(to));
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
        model.addAttribute("active", "dealer-orders");
        model.addAttribute("pageTitle", "EVM • Đơn hàng theo khu vực");

        return "evm/orders/list";
    }

    @GetMapping("/orders/pending")
    public String listPending(RedirectAttributes ra) {
        ra.addAttribute("status", OrderStatus.PENDING_ALLOC.name());
        return "redirect:/evm/orders";
    }

    @GetMapping("/orders/{id}")
    public String detailOrder(@PathVariable(name = "id") Long id, Model model) {
        OrderHdr order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

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
        model.addAttribute("active", "dealer-orders");
        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + id);

        return "evm/orders/detail";
    }

    @PostMapping("/orders/{id}/approve-allocate")
    @Transactional
    public String approveAllocate(@PathVariable(name = "id") Long id, RedirectAttributes ra) {
        OrderHdr o = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        o.setStatus(OrderStatus.ALLOCATED);
        if (o.getAllocatedAt() == null) o.setAllocatedAt(LocalDateTime.now());
        orderRepo.save(o);

        ra.addFlashAttribute("ok", "Đã duyệt cấp xe cho đơn hàng #" + id);
        return "redirect:/evm/orders";
    }

    // ================= PRODUCTS =================
    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Vehicle> vehicles = vehicleRepo.findAll();
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("canMaintainPricing", true);
        model.addAttribute("active", "products");
        model.addAttribute("pageTitle", "Products - EVM Staff View");
        return "evm/products/list";
    }

    // ================= INVENTORY =================
    @GetMapping("/inventory")
    public String listInventory(Model model) {
        List<Inventory> items = inventoryRepo.findAll();
        model.addAttribute("items", items);
        model.addAttribute("active", "inventory");
        model.addAttribute("pageTitle", "EVM Inventory");
        return "evm/inventory/list";
    }

    // ================= PROMOTIONS =================
    @GetMapping("/promotions")
    public String listPromotions(Model model) {
        List<Promotion> list = promotionRepo.findAll();
        model.addAttribute("promotions", list);
        model.addAttribute("active", "promotions");
        model.addAttribute("pageTitle", "EVM Promotions");
        return "dealer/promotions";
    }
}

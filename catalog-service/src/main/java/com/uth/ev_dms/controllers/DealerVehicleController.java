package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.repo.DealerBranchRepo;
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.service.ProductService;
import com.uth.ev_dms.service.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dealer/vehicles")
public class DealerVehicleController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final DealerBranchRepo dealerBranchRepo;
    private final UserRepo userRepo;
    private final VehicleRepo vehicleRepo;

    // =========================================================
    //           LIST XE (DÙNG CHUNG CẢ HAI BẢN)
    // =========================================================
    @GetMapping
    public String list(Model model) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepo.findByUsername(auth.getName()).orElseThrow();

        Long dealerId = Optional.ofNullable(user.getDealer())
                .map(d -> d.getId())
                .orElseThrow(() -> new IllegalStateException("User has no dealer"));

        Long branchId = dealerBranchRepo.findByDealerId(dealerId)
                .orElseThrow(() -> new IllegalStateException("Dealer has no MAIN branch"))
                .getId();

        var vehicles = vehicleRepo.findVehiclesAvailableAtBranch(branchId);

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("activePage", "vehicles");
        model.addAttribute("pageTitle", "Vehicles - Dealer View");

        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEALER_MANAGER"));
        model.addAttribute("canViewPrice", isManager);

        // Dealer *không* xem kho tổng
        model.addAttribute("canViewInventory", false);

        return "dealer/vehicles/list";
    }

    // =========================================================
    //           CHI TIẾT XE (BẢN HOÀN CHỈNH NHẤT)
    // =========================================================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Vehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

        // Lấy user
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Lấy branch từ dealer
        Long branchId = dealerBranchRepo.findByDealerId(user.getDealer().getId())
                .map(b -> b.getId())
                .orElse(null);

        // Map tồn kho trim
        Map<Long, Integer> stockByTrim = new java.util.HashMap<>();
        boolean canViewInventory = false;

        if (branchId != null) {
            // Map: trimId -> quantity
            Map<Long, Integer> raw = inventoryService.getStockByTrimForBranch(branchId);

            // Chỉ lấy tồn kho của trims thuộc model xe
            vehicle.getTrims().forEach(trim ->
                    stockByTrim.put(trim.getId(), raw.getOrDefault(trim.getId(), 0))
            );

            canViewInventory = true;
        }

        // Debug
        stockByTrim.forEach((tid, q) ->
                System.out.println("trimId=" + tid + " qty=" + q)
        );

        // Push data ra view
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("stockByTrim", stockByTrim);
        model.addAttribute("canViewInventory", canViewInventory);
        model.addAttribute("active", "vehicles");
        model.addAttribute("pageTitle", vehicle.getModelName());

        return "dealer/vehicles/detail";
    }

    // =========================================================
    //   API TRẢ VỀ DANH SÁCH TRIM (dùng ở màn tạo báo giá, test-drive…)
    // =========================================================
    @GetMapping("/api/vehicles/{id}/trims")
    @ResponseBody
    public List<Map<String, Object>> getTrims(@PathVariable Long id) {

        Vehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

        return vehicle.getTrims().stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "name", t.getTrimName()
                ))
                .toList();
    }

}

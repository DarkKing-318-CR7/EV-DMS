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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

        boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEALER_MANAGER"));
        model.addAttribute("canViewPrice", isManager);
        // Không hiện màn kho ở Dealer ⇒ false
        model.addAttribute("canViewInventory", false);

        return "dealer/vehicles/list";
    }

//    @GetMapping("/{id}")
//    public String detail(@PathVariable Long id, Model model) {
//        var vehicle = productService.getVehicleById(id);
//        if (vehicle == null) {
//            throw new EntityNotFoundException("Vehicle not found: " + id);
//        }
//
//        Long dealerId = 1L; // tạm
//        var stockByTrim = inventoryService.getStockByTrimForDealer(dealerId);
//
//        System.out.println("=== CONTROLLER stockByTrim ===");
//        stockByTrim.forEach((tid, q) ->
//                System.out.println("trimId=" + tid + " qty=" + q)
//        );
//
//        model.addAttribute("vehicle", vehicle);
//        model.addAttribute("stockByTrim", stockByTrim);
//        model.addAttribute("canViewInventory", true);
//        model.addAttribute("active", "vehicles");
//        model.addAttribute("pageTitle", vehicle.getModelName());
//
//        return "dealer/vehicles/detail";
//    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // 1. Lấy xe
        Vehicle vehicle = vehicleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

        // 2. Lấy user hiện tại
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 3. Lấy branch từ dealer của user (ở đây đang giả sử mỗi dealer có 1 branch)
        Long branchId = dealerBranchRepo.findByDealerId(user.getDealer().getId())
                .map(b -> b.getId())
                .orElse(null);

        // 4. Lấy tồn kho theo branch
        Map<Long, Integer> stockByTrim = new java.util.HashMap<>();
        boolean canViewInventory = false;

        if (branchId != null) {
            // Lấy map: trimId -> qty
            Map<Long, Integer> raw = inventoryService.getStockByTrimForBranch(branchId);

            // Chỉ giữ các trim thuộc vehicle đang xem
            vehicle.getTrims().forEach(trim ->
                    stockByTrim.put(trim.getId(), raw.getOrDefault(trim.getId(), 0))
            );

            canViewInventory = true;
        }

        // Debug nếu muốn
        stockByTrim.forEach((tid, q) ->
                System.out.println("trimId=" + tid + " qty=" + q)
        );

        // 5. Đẩy ra view
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("stockByTrim", stockByTrim);
        model.addAttribute("canViewInventory", canViewInventory);
        model.addAttribute("active", "vehicles");
        model.addAttribute("pageTitle", vehicle.getModelName());

        return "dealer/vehicles/detail";
    }

}

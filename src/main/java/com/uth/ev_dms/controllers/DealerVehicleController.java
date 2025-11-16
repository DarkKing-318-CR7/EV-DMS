package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.service.ProductService;
import com.uth.ev_dms.service.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dealer/vehicles")
public class DealerVehicleController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final VehicleRepo vehicleRepo;

    @GetMapping
    public String list(Model model) {

        var vehicles = productService.getVehiclesWithTrims();

        // TODO: sau này lấy dealerId từ user login
        // Long dealerId = dealerContext.getCurrentDealerId();
        // Map<Long,Integer> stockByTrim = inventoryService.getStockByTrimForDealer(dealerId);

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("stockByTrim", null); // tạm thời null nếu chưa có tồn kho map

        model.addAttribute("active", "vehicles");
        model.addAttribute("pageTitle", "Vehicles - Dealer View");

        // giả lập phân quyền:
        boolean canViewPrice = true;       // Dealer Manager -> true, Dealer Staff -> false
        boolean canViewInventory = true;   // Dealer Manager -> true, Dealer Staff -> false

        model.addAttribute("canViewPrice", canViewPrice);
        model.addAttribute("canViewInventory", canViewInventory);

        return "dealer/vehicles/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var vehicle = productService.getVehicleById(id);
        if (vehicle == null) {
            throw new EntityNotFoundException("Vehicle not found: " + id);
        }

        Long dealerId = 1L; // tạm
        var stockByTrim = inventoryService.getStockByTrimForDealer(dealerId);

        System.out.println("=== CONTROLLER stockByTrim ===");
        stockByTrim.forEach((tid, q) ->
                System.out.println("trimId=" + tid + " qty=" + q)
        );

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("stockByTrim", stockByTrim);
        model.addAttribute("canViewInventory", true);
        model.addAttribute("active", "vehicles");
        model.addAttribute("pageTitle", vehicle.getModelName());

        return "dealer/vehicles/detail";
    }
    @GetMapping("/public/full")
    @ResponseBody
    public List<Vehicle> publicFullVehicles() {
        return vehicleRepo.findAll();
    }
    @GetMapping("/api/vehicles/{id}/trims")
    @ResponseBody
    public List<Map<String, Object>> getTrims(@PathVariable Long id) {

        return vehicleRepo.findById(id)
                .map(v -> v.getTrims().stream().map(t -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", t.getId());
                    m.put("name", t.getTrimName());
                    return m;
                }).toList())
                .orElse(List.of());
    }
}
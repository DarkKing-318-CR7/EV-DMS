package com.uth.ev_dms.controllers;

import com.uth.ev_dms.client.CatalogFeignClient;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.repo.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dealer")
public class DealerController {

    private final CatalogFeignClient catalogClient;
    private final VehicleRepo vehicleRepo;
    private final TrimRepo trimRepo;
    private final OrderRepo orderRepo;
    private final QuoteRepo quoteRepo;
    private final CustomerRepo customerRepo;
    private final TestDriveRepo testDriveRepo;
    private final PromotionRepo promotionRepo;
    private final InventoryRepo inventoryRepo;

    public DealerController(CatalogFeignClient catalogClient,
                            VehicleRepo vehicleRepo,
                            TrimRepo trimRepo,
                            OrderRepo orderRepo,
                            QuoteRepo quoteRepo,
                            CustomerRepo customerRepo,
                            TestDriveRepo testDriveRepo,
                            PromotionRepo promotionRepo,
                            InventoryRepo inventoryRepo) {
        this.catalogClient = catalogClient;
        this.vehicleRepo = vehicleRepo;
        this.trimRepo = trimRepo;
        this.orderRepo = orderRepo;
        this.quoteRepo = quoteRepo;
        this.customerRepo = customerRepo;
        this.testDriveRepo = testDriveRepo;
        this.promotionRepo = promotionRepo;
        this.inventoryRepo = inventoryRepo;
    }

    @GetMapping({"", "/", "/home", "/dashboard"})
    public String dashboard(Model model) {
        long totalOrders = orderRepo.count();
        long pendingOrders = orderRepo.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.NEW || o.getStatus() == OrderStatus.PENDING_ALLOC)
                .count();
        long pendingQuotes = quoteRepo.count();
        long totalVehicles = vehicleRepo.count();
        long totalCustomers = customerRepo.count();
        long testDriveCount = testDriveRepo.count();
        long activePromotions = promotionRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .count();

        List<OrderHdr> recentOrders = orderRepo.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0))
                .limit(5)
                .toList();

        List<TestDrive> recentTestDrives = testDriveRepo.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0))
                .limit(5)
                .toList();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("pendingQuotes", pendingQuotes);
        model.addAttribute("totalVehicles", totalVehicles);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("testDriveCount", testDriveCount);
        model.addAttribute("activePromotions", activePromotions);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("recentTestDrives", recentTestDrives);

        model.addAttribute("pageTitle", "Dealer Dashboard");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("active", "dashboard");
        return "dealer/dashboard";
    }

    @GetMapping("/promotions")
    public String promotions(Model model) {
        model.addAttribute("promotions", promotionRepo.findAll());
        model.addAttribute("active", "promotions");
        model.addAttribute("pageTitle", "Promotions");
        return "dealer/promotions";
    }

    @GetMapping("/vehicles")
    public String vehicles(Model model) {
        try {
            var vehicles = catalogClient.listVehicles();
            if (vehicles != null && !vehicles.isEmpty()) {
                model.addAttribute("vehicles", vehicles);
            } else {
                model.addAttribute("vehicles", vehicleRepo.findAll());
            }
        } catch (Exception e) {
            model.addAttribute("vehicles", vehicleRepo.findAll());
        }
        model.addAttribute("pageTitle", "Vehicles");
        model.addAttribute("active", "vehicles");
        return "dealer/vehicles/list";
    }

    @GetMapping("/vehicles/{id}")
    public String vehicleDetail(@PathVariable("id") Long id, Model model) {
        try {
            var vehicle = catalogClient.getVehicle(id);
            var trims = catalogClient.listTrimsByVehicle(id);
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("trims", trims);
            model.addAttribute("pageTitle", vehicle != null ? vehicle.getModelName() : "Vehicle Detail");
        } catch (Exception e) {
            var vehicle = vehicleRepo.findById(id).orElse(null);
            var trims = trimRepo.findByVehicleId(id);
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("trims", trims);
            model.addAttribute("pageTitle", vehicle != null ? vehicle.getModelName() : "Vehicle Detail");
        }
        model.addAttribute("active", "vehicles");
        return "dealer/vehicles/detail";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        List<com.uth.ev_dms.domain.Inventory> inventories = inventoryRepo.findAll();
        model.addAttribute("inventories", inventories);
        model.addAttribute("active", "inventory");
        model.addAttribute("pageTitle", "Quản Lý Tồn Kho Xe Điện Showroom");
        return "dealer/inventory";
    }
}

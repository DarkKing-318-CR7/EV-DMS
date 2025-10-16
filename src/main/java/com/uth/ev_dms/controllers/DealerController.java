package com.uth.ev_dms.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DealerController {

    // Common
    @GetMapping("/home")
    public String home(Model model){
        model.addAttribute("pageTitle", "Dealer Home");
        model.addAttribute("activePage", "home");
        model.addAttribute("userEmail", "dealer@evdms.com");
        return "home";
    }

    // === Trang quản lý xe ===
    @GetMapping("/dealer/vehicles")
    public String vehicles() {
        return "vehicles";
    }

    @GetMapping("/dealer/vehicle-detail")
    public String vehicleDetail() {
        return "vehicle-detail";
    }

    @GetMapping("/dealer/inventory")
    public String inventory() {
        return "inventory";
    }

    // === Trang khách hàng mới thêm ===
    @GetMapping("/dealer/customers")
    public String customers() {
        return "customers";
    }

    // === Trang lịch hẹn lái thử ===
    @GetMapping("/dealer/test-drive")
    public String testDrive() {
        return "test-drive";
    }

    // === Trang đơn hàng và báo giá ===
    @GetMapping("/dealer/orders")
    public String orders() {
        return "orders";
    }

    @GetMapping("/dealer/quotes")
    public String quotes() {
        return "quotes";
    }

    // === Trang khuyến mãi ===
    @GetMapping("/dealer/promotions")
    public String promotions() {
        return "promotions";
    }

    // === Trang báo cáo ===
    @GetMapping("/dealer/reports")
    public String reports() {
        return "reports";
    }

    // === Trang hồ sơ cá nhân ===
    @GetMapping("/dealer/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home"; // chuyển hướng sang trang /home
    }

}
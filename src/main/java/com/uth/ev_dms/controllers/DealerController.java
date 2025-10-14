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

    @GetMapping("/login") public String login(){ return "login"; }
    @GetMapping("/forgot") public String forgot(){ return "forgot"; }
    @GetMapping("/profile") public String profile(){ return "profile"; }
    @GetMapping("/settings") public String settings(){ return "settings"; }
    @GetMapping("/support") public String support(){ return "support"; }

    // Dealer Staff
    @GetMapping("/vehicles") public String vehicles(){ return "vehicles"; }
    @GetMapping("/vehicle-detail") public String vehicleDetail(){ return "vehicle-detail"; }
    @GetMapping("/quotes") public String quotes(){ return "quotes"; }
    @GetMapping("/quote-create") public String quoteCreate(){ return "quote-create"; }
    @GetMapping("/orders") public String orders(){ return "orders"; }
    @GetMapping("/order-create") public String orderCreate(){ return "order-create"; }
    @GetMapping("/customers") public String customers(){ return "customers"; }
    @GetMapping("/customer-detail") public String customerDetail(){ return "customer-detail"; }
    @GetMapping("/test-drive") public String testDrive(){ return "test-drive"; }
    @GetMapping("/payments") public String payments(){ return "payments"; }
    @GetMapping("/invoices") public String invoices(){ return "invoices"; }

    // Dealer Manager
    @GetMapping("/approvals") public String approvals(){ return "approvals"; }
    @GetMapping("/dealer-reports") public String dealerReports(){ return "dealer-reports"; }

    // EVM Staff
    @GetMapping("/catalog") public String catalog(){ return "catalog"; }
    @GetMapping("/central-warehouse") public String centralWarehouse(){ return "central-warehouse"; }
    @GetMapping("/allocation") public String allocation(){ return "allocation"; }
    @GetMapping("/wholesale-pricing") public String wholesalePricing(){ return "wholesale-pricing"; }
    @GetMapping("/campaigns") public String campaigns(){ return "campaigns"; }
    @GetMapping("/forecast") public String forecast(){ return "forecast"; }
    @GetMapping("/dealer-management") public String dealerManagement(){ return "dealer-management"; }

    // Admin
    @GetMapping("/admin-dashboard") public String adminDashboard(){ return "admin-dashboard"; }
    @GetMapping("/user-admin") public String userAdmin(){ return "user-admin"; }
    @GetMapping("/system-config") public String systemConfig(){ return "system-config"; }
    @GetMapping("/audit-logs") public String auditLogs(){ return "audit-logs"; }
    @GetMapping("/backup-restore") public String backupRestore(){ return "backup-restore"; }
    @GetMapping("/notifications") public String notifications(){ return "notifications"; }

    // Optionally: promotions & reports (common)
    @GetMapping("/promotions") public String promotions(){ return "promotions"; }
    @GetMapping("/reports") public String reports(){ return "reports"; }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("dealers",    dashboardService.totalDealers());
        model.addAttribute("vehicles",   dashboardService.totalVehicles());
        model.addAttribute("trims",      dashboardService.totalTrims());
        model.addAttribute("promotions", dashboardService.totalPromotions());
        model.addAttribute("orders",     dashboardService.totalOrders());
        model.addAttribute("inventory",  dashboardService.totalInventory());
        model.addAttribute("users",      dashboardService.totalUsers());
        model.addAttribute("customers",  dashboardService.totalCustomers());
        model.addAttribute("quotes",     dashboardService.totalQuotes());
        model.addAttribute("revenue",    dashboardService.totalRevenue());

        return "admin/dashboard";
    }
}

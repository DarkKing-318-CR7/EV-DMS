package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Dashboard of Reports
    @GetMapping("/reports")
    public String reportDashboard() {
        return "reports/report";
    }

    // Sales Report page
    @GetMapping("/reports/sale")
    public String salesReport(Model model) {
        model.addAttribute("sales", reportService.getSalesByDay(30));
        return "reports/sale";
    }

    // Vehicles Inventory Report page
    @GetMapping("/reports/vehicles")
    public String vehicleReport(Model model) {
        model.addAttribute("vehicles", reportService.getVehiclesInventory());
        return "reports/vehicles";
    }

    // Top Selling Trims (Custom Report Example)
    @GetMapping("/reports/custom")
    public String customReport(Model model) {
        model.addAttribute("topTrims", reportService.getTopSellingTrims(10));
        return "reports/custom";
    }
}

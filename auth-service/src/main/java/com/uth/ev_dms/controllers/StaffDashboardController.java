package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.StaffDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffDashboardController {

    private final StaffDashboardService staffDashboardService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        try {
            model.addAttribute("hotCars", staffDashboardService.getHotModelsThisWeek());
            model.addAttribute("latestCustomers", staffDashboardService.latestCustomers());
            model.addAttribute("todayTestDrives", staffDashboardService.todayTestDrive());
        } catch (Exception e) {
            model.addAttribute("hotCars", java.util.Collections.emptyList());
            model.addAttribute("latestCustomers", java.util.Collections.emptyList());
            model.addAttribute("todayTestDrives", java.util.Collections.emptyList());
        }
        model.addAttribute("active", "dashboard");
        model.addAttribute("pageTitle", "Staff Dashboard");
        return "staff/dashboard";
    }
}

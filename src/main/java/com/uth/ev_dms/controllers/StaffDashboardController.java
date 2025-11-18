package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.StaffDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff")
public class StaffDashboardController {

    private final StaffDashboardService service;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {

        model.addAttribute("hotCars", service.getHotModelsThisWeek());
        model.addAttribute("recentCustomers", service.latestCustomers());
        model.addAttribute("todayTestDrives", service.todayTestDrive());

        return "staff/dashboard";
    }
}

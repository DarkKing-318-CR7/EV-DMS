package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Demo — sau này bạn sẽ gọi sang report-service để lấy dữ liệu thật
        model.addAttribute("dealers", 5);
        model.addAttribute("vehicles", 12);
        model.addAttribute("trims", 20);
        model.addAttribute("promotions", 4);
        model.addAttribute("orders", 10);
        model.addAttribute("inventory", 78);
        model.addAttribute("users", 6);
        model.addAttribute("customers", 30);
        model.addAttribute("quotes", 15);
        model.addAttribute("revenue", 200000000);

        return "admin/dashboard"; // trỏ đúng file dashboard.html
    }
}

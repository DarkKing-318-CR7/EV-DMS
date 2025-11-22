package com.uth.ev_dms.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.uth.ev_dms.service.EvmDashboardService;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class EvmDashboardController {

    private final EvmDashboardService dashboardService;

    @GetMapping("/evm/dashboard")
    public String dashboard(Model model) {

        Map<String, Object> stats = new HashMap<>();
        stats.put("dealers", 2);
        stats.put("vehicles", 13);
        stats.put("trims", 13);
        stats.put("promotions", 4);
        stats.put("orders", 10);
        stats.put("inventory", 78);
        stats.put("users", 6);
        stats.put("customers", 0);
        stats.put("quotes", 20);

        // 🔥 Doanh thu thật từ database
        stats.put("revenue", dashboardService.getRevenue());

        model.addAttribute("stats", stats);

        model.addAttribute("recent", List.of(
                "Đăng ký đại lý mới",
                "Hợp đồng được phê duyệt",
                "Phân bổ xe mới"
        ));

        return "evm/dashboard";
    }
}

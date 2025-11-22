package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.DealerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class DealerDashboardController {

    private final DealerDashboardService dashboardService;

    // === MANAGER DASHBOARD ===
    @GetMapping("/dealer/dashboard-manager")
    public String dashboardManager(Model model, Principal principal) {

        String username = principal.getName();
        Long dealerId = dashboardService.getDealerIdByUsername(username);

        model.addAttribute("stats", dashboardService.getDealerStats(dealerId));
        model.addAttribute("inventoryByModel", dashboardService.getInventoryByModel(dealerId));
        model.addAttribute("lowStock", dashboardService.getLowStockModels(dealerId));

        // đúng thư mục: templates/manager/dashboard.html
        return "manager/dashboard";
    }

    // === STAFF DASHBOARD ===
    @GetMapping("/dealer/dashboard")
    public String dashboardStaff(Model model, Principal principal) {

        String username = principal.getName();
        Long dealerId = dashboardService.getDealerIdByUsername(username);

        model.addAttribute("stats", dashboardService.getDealerStats(dealerId));
        model.addAttribute("inventoryByModel", dashboardService.getInventoryByModel(dealerId));
        model.addAttribute("lowStock", dashboardService.getLowStockModels(dealerId));

        // đúng thư mục: templates/staff/dashboard.html
        return "staff/dashboard";
    }
}

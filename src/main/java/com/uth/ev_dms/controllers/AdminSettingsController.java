package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminSettingsController {

    // method /admin/settings cũ của bạn giữ nguyên
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Admin Settings");
        model.addAttribute("active", "settings");
        return "admin/settings";
    }

    // 👉 THÊM MỚI: trang System Parameters
    @GetMapping("/settings/system")
    public String systemParameters(Model model) {
        model.addAttribute("pageTitle", "System Parameters");
        model.addAttribute("active", "settings");

        // demo vài tham số, sau này bạn có thể load từ DB
        model.addAttribute("maxUsersPerDealer", 50);
        model.addAttribute("defaultCurrency", "VND");
        model.addAttribute("sessionTimeoutMinutes", 30);

        return "admin/settings-system";
    }
}

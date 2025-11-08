package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dealer")
public class DealerController {

    @GetMapping({"", "/", "/home"})
    public String home(Model model) {
        model.addAttribute("pageTitle", "Dealer Home");
        model.addAttribute("activePage", "home");
        return "dealer/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dealer Dashboard");
        model.addAttribute("activePage", "dashboard");
        return "dealer/dashboard";
    }

//    @GetMapping("/vehicles") public String vehicles() { return "dealer/vehicles"; }
    @GetMapping("/customers") public String customers() { return "dealer/customers"; }
    @GetMapping("/test-drive") public String testDrive() { return "dealer/test-drive"; }
    @GetMapping("/quotes") public String quotes() { return "dealer/quotes"; }


    @GetMapping("/promotions") public String promotions() { return "dealer/promotions"; }
    @GetMapping("/reports") public String reports() { return "dealer/reports"; }
    @GetMapping("/profile") public String profile() { return "common/profile"; }
}

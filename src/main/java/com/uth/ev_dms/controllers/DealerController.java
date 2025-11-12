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
}

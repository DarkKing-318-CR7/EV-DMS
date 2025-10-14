package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {

    @GetMapping("/login")
    public String home(Model model) {
        model.addAttribute("title", "Electric Vehicle Dealer Management System");
        model.addAttribute("subtitle", "EV-DMS Dashboard Prototype");
        model.addAttribute("message", "Welcome to the Electric Vehicle Dealer Management System!");
        return "login";
    }
}

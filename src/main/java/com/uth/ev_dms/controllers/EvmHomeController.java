package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EvmHomeController {

    @GetMapping("/evm/home")
    public String showEvmHome(Model model) {
        // demo data – sau này có thể thay bằng service thực
        model.addAttribute("totalPromotions", 4);
        model.addAttribute("activePromotions", 3);
        return "evm/home"; // -> templates/evm/home.html
    }
}

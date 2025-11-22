package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EvmHomeController {

    @GetMapping("/evm/home")
    public String home() {
        return "redirect:/evm/dashboard";
    }
}


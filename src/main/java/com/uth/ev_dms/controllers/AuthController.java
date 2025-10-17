package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/")
    public String root() { return "redirect:/login"; } // hoặc "home" public nếu bạn muốn

    @GetMapping("/logout")
    public String logout(){
        return "login";
    }
}

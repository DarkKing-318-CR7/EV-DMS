package com.uth.ev_dms.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";            // templates/login.html
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Authentication auth) {
        if (auth != null) {
           /* if (hasRole(auth, "ADMIN"))  return "redirect:/admin/dashboard";*/
            if (hasRole(auth, "DEALER")) return "redirect:/dealer/home";
        }
        return "home";             // templates/home.html (trang public)
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_" + role));
    }
}

package com.uth.ev_dms.wed;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostLoginController {

    @GetMapping("/post-login")
    public String postLogin(Authentication auth) {
        var hasAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_EVM"));
        if (hasAdmin) return "redirect:/admin/dashboard";
        return "redirect:/dealer/home";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }
}

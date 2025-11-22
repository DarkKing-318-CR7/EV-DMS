package com.uth.ev_dms.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostLoginController {

    @GetMapping("/post-login")
    public String postLogin(Authentication auth) {

        if (auth == null) return "redirect:/login";

        // === Admin ===
        if (hasRole(auth, "ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        // === Dealer Manager ===
        if (hasRole(auth, "DEALER_MANAGER")) {
            return "redirect:/dealer/dashboard-manager";
        }

        // === Dealer Staff ===
        if (hasRole(auth, "DEALER_STAFF")) {
            return "redirect:/dealer/dashboard";
        }

        // === EVM Staff (tùy logic) ===
        if (hasRole(auth, "EVM_STAFF")) {
            return "redirect:/evm/dashboard";
        }

        // fallback → staff
        return "redirect:/dealer/dashboard";
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_" + role));
    }
}

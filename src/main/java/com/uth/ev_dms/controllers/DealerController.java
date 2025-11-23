package com.uth.ev_dms.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dealer")
public class DealerController {

    @GetMapping({"", "/", "/home"})
    public String home(Authentication auth) {

        if (auth == null) return "redirect:/login";

        // MANAGER
        if (hasRole(auth, "DEALER_MANAGER")) {
            return "manager/dashboard";
        }

        // STAFF
        if (hasRole(auth, "DEALER_STAFF")) {
            return "staff/dashboard";
        }

        return "staff/dashboard";
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_" + role));
    }
}

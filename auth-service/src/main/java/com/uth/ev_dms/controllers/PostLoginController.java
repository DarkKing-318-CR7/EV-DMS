//package com.uth.ev_dms.controllers;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//public class PostLoginController {
//
//    @GetMapping("/post-login")
//    public String postLogin(Authentication auth) {
//        if (auth == null) return "redirect:/login";
//        boolean isAdmin = hasRole(auth, "ADMIN");
//        if (isAdmin) return "redirect:/admin/dashboard";
//        // còn lại vào dealer
//        return "redirect:/dealer/home";
//    }
//
//    private boolean hasRole(Authentication auth, String role) {
//        return auth.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .anyMatch(a -> a.equals("ROLE_" + role));
//    }
//}



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

        // 1) Admin -> dashboard admin
        if (hasRole(auth, "ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        // 2) EVM staff -> dashboard EVM
        if (hasRole(auth, "EVM_STAFF")) {
            return "redirect:/evm/dashboard";
        }

        // 3) Dealer Manager -> trang chủ Dealer
        if (hasRole(auth, "DEALER_MANAGER")) {
            return "redirect:/dealer/home";
        }

        // 4) Dealer Staff -> dashboard Staff
        if (hasRole(auth, "DEALER_STAFF")) {
            return "redirect:/staff/dashboard";
        }

        return "redirect:/dealer/home";
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_" + role));
    }
}

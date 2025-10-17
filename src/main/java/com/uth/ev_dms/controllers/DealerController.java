package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/dealer") // prefix chung cho toan bo trang dealer
public class DealerController {

    // home dealer: /dealer, /dealer/, /dealer/home
    @GetMapping({"", "/", "/home"})
    public String home(Model model, Principal principal) {
        model.addAttribute("pageTitle", "Dealer Home");
        model.addAttribute("activePage", "home");
        if (principal != null) {
            model.addAttribute("userEmail", principal.getName()); // lấy email đăng nhập
        } else {
            model.addAttribute("userEmail", "dealer@example.com");
        }

        return "dealer/home";
    }

    // quan ly xe
    @GetMapping("/vehicles")
    public String vehicles() { return "dealer/vehicles"; }

    @GetMapping("/vehicle-detail")
    public String vehicleDetail() { return "dealer/vehicle-detail"; }

    @GetMapping("/inventory")
    public String inventory() { return "dealer/inventory"; }

    // khach hang
    @GetMapping("/customers")
    public String customers() { return "dealer/customers"; }

    // lich lai thu
    @GetMapping("/test-drive")
    public String testDrive() { return "dealer/test-drive"; }

    // don hang va bao gia
    @GetMapping("/orders")
    public String orders() { return "dealer/orders"; }

    @GetMapping("/quotes")
    public String quotes() { return "dealer/quotes"; }

    // khuyen mai
    @GetMapping("/promotions")
    public String promotions() { return "dealer/promotions"; }

    // bao cao
    @GetMapping("/reports")
    public String reports() { return "dealer/reports"; }

    // ho so ca nhan (neu dat o common thi doi thanh "common/profile")
    @GetMapping("/profile")
    public String profile() { return "common/profile"; }
}

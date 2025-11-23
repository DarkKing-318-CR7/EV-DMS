package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager/customers")
public class ManagerCustomerPageController {

    @GetMapping
    public String page() {
        // Dùng lại giao diện Dealer
        return "dealer/customers";
    }
}

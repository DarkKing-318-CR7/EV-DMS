package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/dealer/customers")
@RequiredArgsConstructor
public class DealerCustomerController {

    private final CustomerService service;

    // Staff thấy "My"
    @GetMapping("/my")
    public String myList(@RequestParam(defaultValue="0") int page,
                         @RequestParam(defaultValue="10") int size,
                         Model model, Principal p) {
        model.addAttribute("page", service.myList(p.getName(), page, size));
        return "dealer/customers/my-list";
    }

    // form + create/update đơn giản (tùy bạn làm thêm)
}


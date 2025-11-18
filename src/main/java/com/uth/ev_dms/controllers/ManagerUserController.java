package com.uth.ev_dms.controllers;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.service.DealerUserService;
import com.uth.ev_dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/users")
public class ManagerUserController {

    private final DealerUserService dealerUserService;
    private final UserService userService; // security helper

    @GetMapping
    public String list(Model model, Principal principal) {
        Long dealerId = userService.getDealerId(principal);
        model.addAttribute("staff", dealerUserService.findStaff(dealerId));
        return "manager/users/list";
    }

    @GetMapping("/create")
    public String form(Model model) {
        model.addAttribute("user", new User());
        return "manager/users/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute User user, Principal principal) {

        Long dealerId = userService.getDealerId(principal);
        dealerUserService.createStaff(dealerId, user);

        return "redirect:/manager/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Principal principal) {

        Long dealerId = userService.getDealerId(principal);
        dealerUserService.toggleStaffStatus(dealerId, id);

        return "redirect:/manager/users";
    }
}

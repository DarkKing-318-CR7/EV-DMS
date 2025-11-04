package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.TestDriveService;
import com.uth.ev_dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/testdrives")
public class StaffTestDriveController {

    private final TestDriveService testDriveService;
    private final UserService userService;

    @GetMapping
    public String list(Model model, Principal principal) {
        Long staffId = userService.findIdByUsername(principal.getName());
        model.addAttribute("list", testDriveService.findMineAssigned(staffId));
        return "staff/testdrives"; // trỏ tới templates/staff/testdrives.html
    }
}

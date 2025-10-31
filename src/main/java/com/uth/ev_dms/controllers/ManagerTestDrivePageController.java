package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.TestDriveStatus;
import com.uth.ev_dms.service.TestDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/testdrives")
public class ManagerTestDrivePageController {

    private final TestDriveService testDriveService;

    @GetMapping
    public String list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(required = false) TestDriveStatus status,
            Model model
    ) {
        model.addAttribute("list", testDriveService.list(from, to, status));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("status", status);
        return "manager/testdrives"; // must match template path below
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        testDriveService.approve(id);
        return "redirect:/manager/testdrives";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        testDriveService.cancel(id);
        return "redirect:/manager/testdrives";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id) {
        testDriveService.complete(id);
        return "redirect:/manager/testdrives";
    }
}

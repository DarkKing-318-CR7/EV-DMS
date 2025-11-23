package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.TestDriveStatus;
import com.uth.ev_dms.service.TestDriveService;
import com.uth.ev_dms.service.UserService;   // ⭐ THÊM
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;   // ⭐ THÊM
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/testdrives")
public class ManagerTestDrivePageController {

    private final TestDriveService testDriveService;
    private final UserService userService;   // ⭐ THÊM

    @GetMapping
    public String list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(required = false) TestDriveStatus status,
            Principal principal,       // ⭐ THÊM
            Model model
    ) {
        Long dealerId = userService.getDealerId(principal);  // ⭐ THÊM

        model.addAttribute("list",
                testDriveService.listByDealer(dealerId, from, to, status)  // ⭐ THÊM
        );

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("status", status);

        return "manager/testdrives";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Principal principal) {  // ⭐ THÊM Principal
        Long dealerId = userService.getDealerId(principal);  // ⭐ THÊM

        if (!testDriveService.get(id).getDealer().getId().equals(dealerId))   // ⭐ THÊM
            throw new RuntimeException("Bạn không thể duyệt lịch của đại lý khác!");

        testDriveService.approve(id);
        return "redirect:/manager/testdrives";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal) {   // ⭐ THÊM
        Long dealerId = userService.getDealerId(principal);   // ⭐ THÊM

        if (!testDriveService.get(id).getDealer().getId().equals(dealerId))  // ⭐ THÊM
            throw new RuntimeException("Bạn không thể hủy lịch của đại lý khác!");

        testDriveService.cancel(id);
        return "redirect:/manager/testdrives";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, Principal principal) {  // ⭐ THÊM
        Long dealerId = userService.getDealerId(principal);  // ⭐ THÊM

        if (!testDriveService.get(id).getDealer().getId().equals(dealerId))  // ⭐ THÊM
            throw new RuntimeException("Bạn không thể hoàn tất lịch của đại lý khác!");

        testDriveService.complete(id);
        return "redirect:/manager/testdrives";
    }
}

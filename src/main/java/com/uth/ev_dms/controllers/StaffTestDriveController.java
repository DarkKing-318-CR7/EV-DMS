package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.TestDriveService;
import com.uth.ev_dms.service.UserService;
import com.uth.ev_dms.service.CustomerService;
import com.uth.ev_dms.service.ProductService;

import com.uth.ev_dms.service.dto.TestDriveCreateForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/testdrives")
public class StaffTestDriveController {

    private final TestDriveService testDriveService;
    private final UserService userService;
    private final CustomerService customerService;  // ✔ thêm
    private final ProductService productService;    // ✔ thêm

    @GetMapping
    public String list(Model model, Principal principal) {
        Long staffId = userService.findIdByUsername(principal.getName());
        model.addAttribute("list", testDriveService.findMineAssigned(staffId));
        return "dealer/testdrive/list";
    }

    @GetMapping("/create")
    public String createPage(Model model) {

        model.addAttribute("customers", customerService.findAll()); // ✔ đúng
        model.addAttribute("vehicles", productService.getVehiclesWithTrims()); // ✔ đúng

        return "dealer/testdrive/create-staff";
    }
    @PostMapping("/create")
    public String submitCreate(
            @ModelAttribute TestDriveCreateForm form,
            Principal principal,
            RedirectAttributes ra
    ) {
        Long staffId = userService.findIdByUsername(principal.getName());

        try {
            testDriveService.createByStaff(form, staffId);
            ra.addFlashAttribute("msg", "Tạo lịch lái thử thành công!");
            return "redirect:/staff/testdrives";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/testdrives/create";
        }
    }
}

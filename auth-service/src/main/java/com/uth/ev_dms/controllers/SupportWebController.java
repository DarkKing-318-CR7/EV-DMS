package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupportWebController {

    @GetMapping({"/support", "/admin/support"})
    public String supportForm(Model model) {
        model.addAttribute("active", "support");
        model.addAttribute("pageTitle", "Hỗ trợ kỹ thuật");
        return "admin/support";
    }

    @PostMapping({"/support", "/admin/support"})
    public String submitSupport(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String message,
            RedirectAttributes ra
    ) {
        ra.addFlashAttribute("success", "Yêu cầu hỗ trợ của bạn đã được gửi thành công. Chúng tôi sẽ phản hồi sớm!");
        return "redirect:/support";
    }
}

package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupportController {

    @GetMapping("/support")
    public String supportForm(Model model) {
        model.addAttribute("pageTitle", "Support");
        model.addAttribute("active", "support"); // dùng cho menu active nếu cần
        return "admin/support";                  // dùng file templates/admin/support.html
    }

    @PostMapping("/support")
    public String submitSupport(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String message,
                                RedirectAttributes ra) {

        // TODO: sau này có thể lưu DB / gửi email
        System.out.println("Support request from " + name + " (" + email + "): " + message);

        ra.addFlashAttribute("success",
                "Yêu cầu hỗ trợ đã được gửi. Chúng tôi sẽ phản hồi sớm nhất.");
        return "redirect:/support";
    }
}

package com.uth.ev_dms.support;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SupportController {

    @GetMapping("/support")
    public String supportForm(Model model) {
        model.addAttribute("supportRequest", new SupportRequest());
        return "support";
    }

    @PostMapping("/support")
    public String submitSupport(@ModelAttribute SupportRequest request, Model model) {
        System.out.println("📩 Support request: " + request.getMessage());
        model.addAttribute("success", "Yêu cầu của bạn đã được gửi!");
        return "support";
    }
}

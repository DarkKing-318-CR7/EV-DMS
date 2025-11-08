package com.uth.ev_dms.support;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SupportController {
    private final SupportRepository repo;
    public SupportController(SupportRepository repo) { this.repo = repo; }

    @GetMapping("/support")
    public String supportForm(Model model) {
        model.addAttribute("ticket", new SupportTicket());
        return "support/support";
    }

    @PostMapping("/support")
    public String submitSupport(@ModelAttribute SupportTicket ticket, Model model) {
        repo.save(ticket);
        model.addAttribute("success", "Yêu cầu đã gửi. Chúng tôi sẽ trả lời sớm.");
        model.addAttribute("ticket", new SupportTicket());
        return "support/support";
    }

    // admin view
    @GetMapping("/admin/support")
    public String viewTickets(Model model) {
        model.addAttribute("tickets", repo.findAll());
        return "support/list";
    }
}

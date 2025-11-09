package com.uth.ev_dms.audit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditController {
    private final AuditService service;
    public AuditController(AuditService service) { this.service = service; }

    @GetMapping("/admin/audit")
    public String audit(Model model) {
        model.addAttribute("logs", service.findAll());
        return "audit/audit";
    }
}

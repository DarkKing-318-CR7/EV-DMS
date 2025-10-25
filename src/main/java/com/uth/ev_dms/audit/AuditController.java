package com.uth.ev_dms.audit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/audit")
    public String viewAuditLogs(Model model) {
        model.addAttribute("logs", auditService.getAll());
        return "audit";
    }
}

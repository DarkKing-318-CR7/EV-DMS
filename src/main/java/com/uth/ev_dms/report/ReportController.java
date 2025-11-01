package com.uth.ev_dms.report;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAllAttributes(service.getSalesReport());
        return "reports/report";
    }
}

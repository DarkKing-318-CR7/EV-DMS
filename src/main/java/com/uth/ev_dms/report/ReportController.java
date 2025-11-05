package com.uth.ev_dms.report;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String getReports(Model model) {
        model.addAttribute("salesReport", reportService.getSalesReport());
        return "reports/report";
    }
}

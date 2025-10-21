package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.service.SalesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dealer/quotes-mvc")
public class QuoteMvcController {

    private final SalesService salesService;

    public QuoteMvcController(SalesService salesService) {
        this.salesService = salesService;
    }

    // STAFF: xem danh sách của mình
    @GetMapping("/my")
    public String myQuotes(Model model) {
        List<Quote> quotes = salesService.findAll(); // hoặc salesService.findByUser(...)
        model.addAttribute("quotes", quotes);
        model.addAttribute("role", "STAFF");
        return "dealer/quotes";
    }

    // MANAGER: xem danh sách pending
    @GetMapping("/pending")
    public String pendingQuotes(Model model) {
        List<Quote> quotes = salesService.findPending();
        model.addAttribute("quotes", quotes);
        model.addAttribute("role", "MANAGER");
        return "dealer/quotes";
    }

    // Submit (staff)
    @PostMapping("/submit/{id}")
    public String submit(@PathVariable Long id) {
        salesService.submitQuote(id);
        return "redirect:/dealer/quotes/my";
    }

    // Approve (manager)
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        salesService.approveQuote(id);
        return "redirect:/dealer/quotes/pending";
    }

    // Reject (manager)
    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, @RequestParam String comment) {
        salesService.rejectQuote(id, comment);
        return "redirect:/dealer/quotes/pending";
    }
}

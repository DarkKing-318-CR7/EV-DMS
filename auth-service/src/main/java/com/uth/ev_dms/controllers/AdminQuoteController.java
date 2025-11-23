package com.uth.ev_dms.controllers;

import com.uth.ev_dms.client.SalesClient;
import com.uth.ev_dms.client.dto.QuoteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/quotes")
@RequiredArgsConstructor
public class AdminQuoteController {

    private final SalesClient salesClient;

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       Model model) {
        List<QuoteDto> quotes = salesClient.getQuotes(status);
        model.addAttribute("quotes", quotes);
        model.addAttribute("status", status);
        return "admin/quotes/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        QuoteDto quote = salesClient.getQuote(id);
        model.addAttribute("quote", quote);
        return "admin/quotes/detail";
    }
}

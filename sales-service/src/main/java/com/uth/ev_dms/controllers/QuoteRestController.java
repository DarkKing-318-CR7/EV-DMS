package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.repo.QuoteRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/quotes")
@RequiredArgsConstructor
public class QuoteRestController {

    private final QuoteRepo quoteRepo;

    // Lấy tất cả quote (Admin xem pipeline toàn hệ thống)
    @GetMapping
    public List<Quote> getAllQuotes(
            @RequestParam(required = false) String status
    ) {
        if (status != null && !status.isBlank()) {
            return quoteRepo.findByStatus(status);
        }
        return quoteRepo.findAll();
    }

    // Lấy chi tiết 1 quote
    @GetMapping("/{id}")
    public Quote getQuoteById(@PathVariable Long id) {
        return quoteRepo.findById(id).orElseThrow();
    }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.service.SalesService;
import com.uth.ev_dms.service.PromotionService;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/dealer/quotes")
public class DealerQuoteController {

    private final SalesService salesService;
    private final PromotionService promotionService;

    public DealerQuoteController(SalesService salesService, PromotionService promotionService) {
        this.salesService = salesService;
        this.promotionService = promotionService;
    }

    // ================= STAFF =================

    // danh sách quote của staff
    @GetMapping("/my")
    public String myQuotes(Model model) {
        List<Quote> quotes = salesService.findAll(); // có thể thay bằng findByDealerId(...)
        model.addAttribute("quotes", quotes);
        model.addAttribute("role", "STAFF"); // để quotes.html biết hiển thị nút Submit
        return "dealer/quotes";
    }

    // form tạo quote
    @GetMapping("/my/new")
    public String createForm(Model model) {
        model.addAttribute("quote", new CreateQuoteDTO());

        // nếu PromotionService đã có validate theo region/dealer/trim thì điền tham số phù hợp
        List<Promotion> promos = promotionService.getValidPromotions(null, null, null, LocalDate.now());
        model.addAttribute("promotions", promos);

        return "dealer/quote-create";
    }

    // lưu quote (và áp dụng promotions nếu có)
    @PostMapping("/my/save")
    public String saveQuote(@ModelAttribute CreateQuoteDTO dto,
                            @RequestParam(value = "promotionIds", required = false) List<Long> promotionIds) {
        Quote q = salesService.createQuote(dto);
        if (promotionIds != null && !promotionIds.isEmpty()) {
            salesService.applyPromotions(q.getId(), promotionIds);
        }
        return "redirect:/dealer/quotes/my";
    }

    // submit quote => PENDING
    @PostMapping("/submit/{id}")
    public String submitQuote(@PathVariable Long id) {
        salesService.submitQuote(id);
        return "redirect:/dealer/quotes/my";
    }

    // ================ MANAGER (bước 2) ================
    @GetMapping("/pending")
    public String pending(Model model) {
        model.addAttribute("quotes", salesService.findPending());
        model.addAttribute("role", "MANAGER"); // để quotes.html biết hiển thị Approve/Reject
        return "dealer/quotes";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        salesService.approveQuote(id);
        return "redirect:/dealer/quotes/pending";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, @RequestParam String comment) {
        salesService.rejectQuote(id, comment);
        return "redirect:/dealer/quotes/pending";
    }
}

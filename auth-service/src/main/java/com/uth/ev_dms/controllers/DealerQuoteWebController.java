package com.uth.ev_dms.controllers;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.repo.QuoteRepo;
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dealer/quotes")
@RequiredArgsConstructor
public class DealerQuoteWebController {

    private final QuoteRepo quoteRepo;
    private final UserRepo userRepo;

    private User getCurrentUser(Authentication auth) {
        if (auth == null) return null;
        return userRepo.findByUsername(auth.getName()).orElse(null);
    }

    private boolean isManager(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_DEALER_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ================= LIST QUOTES =================
    @GetMapping({"", "/"})
    public String index(Model model, Authentication auth) {
        if (isManager(auth)) {
            return "redirect:/dealer/quotes/pending";
        }
        return "redirect:/dealer/quotes/my";
    }

    @GetMapping("/my")
    public String myQuotes(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        Long dealerId = (user != null && user.getDealer() != null) ? user.getDealer().getId() : 1L;

        List<Quote> quotes = quoteRepo.findByDealerId(dealerId);
        if (quotes.isEmpty()) {
            quotes = quoteRepo.findAll();
        }

        model.addAttribute("quotes", quotes);
        model.addAttribute("role", isManager(auth) ? "MANAGER" : "STAFF");
        model.addAttribute("active", "quotes");
        model.addAttribute("pageTitle", "Báo giá của tôi");
        return "dealer/quotes";
    }

    @GetMapping("/pending")
    public String pendingQuotes(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        Long dealerId = (user != null && user.getDealer() != null) ? user.getDealer().getId() : 1L;

        List<Quote> quotes = quoteRepo.findByDealerIdAndStatus(dealerId, "PENDING");
        if (quotes.isEmpty()) {
            quotes = quoteRepo.findByStatus("PENDING");
            if (quotes.isEmpty()) {
                quotes = quoteRepo.findAll();
            }
        }

        model.addAttribute("quotes", quotes);
        model.addAttribute("role", "MANAGER");
        model.addAttribute("active", "quotes");
        model.addAttribute("pageTitle", "Báo giá chờ duyệt");
        return "dealer/quotes";
    }

    // ================= CREATE FORM =================
    @GetMapping("/my/new")
    public String newQuoteForm(Model model, Authentication auth) {
        CreateQuoteDTO dto = new CreateQuoteDTO();
        dto.setStatus("DRAFT");
        model.addAttribute("quote", dto);
        model.addAttribute("active", "quotes");
        model.addAttribute("pageTitle", "Tạo báo giá mới");
        return "dealer/quote-create";
    }

    @PostMapping("/my/save")
    @Transactional
    public String saveQuote(@ModelAttribute("quote") CreateQuoteDTO dto,
                            Authentication auth,
                            RedirectAttributes ra) {
        User user = getCurrentUser(auth);
        Long dealerId = (user != null && user.getDealer() != null) ? user.getDealer().getId() : 1L;

        Quote q = new Quote();
        q.setCustomerId(dto.getCustomerId());
        q.setDealerId(dealerId);
        q.setStatus(dto.getStatus() != null ? dto.getStatus() : "DRAFT");
        q.setTotalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO);
        q.setAppliedDiscount(BigDecimal.ZERO);
        q.setFinalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO);
        q.setCreatedAt(LocalDateTime.now());

        quoteRepo.save(q);
        ra.addFlashAttribute("ok", "Tạo báo giá thành công.");
        return "redirect:/dealer/quotes/my";
    }

    // ================= ACTIONS =================
    @PostMapping("/submit/{id}")
    @Transactional
    public String submitQuote(@PathVariable Long id, RedirectAttributes ra) {
        Quote q = quoteRepo.findById(id).orElse(null);
        if (q != null) {
            q.setStatus("PENDING");
            quoteRepo.save(q);
            ra.addFlashAttribute("ok", "Đã gửi báo giá lên quản lý duyệt.");
        }
        return "redirect:/dealer/quotes/my";
    }

    @PostMapping("/approve/{id}")
    @Transactional
    public String approveQuote(@PathVariable Long id, RedirectAttributes ra) {
        Quote q = quoteRepo.findById(id).orElse(null);
        if (q != null) {
            q.setStatus("APPROVED");
            quoteRepo.save(q);
            ra.addFlashAttribute("ok", "Đã duyệt báo giá.");
        }
        return "redirect:/dealer/quotes/pending";
    }

    @PostMapping("/reject/{id}")
    @Transactional
    public String rejectQuote(@PathVariable Long id,
                              @RequestParam(value = "comment", required = false) String comment,
                              RedirectAttributes ra) {
        Quote q = quoteRepo.findById(id).orElse(null);
        if (q != null) {
            q.setStatus("REJECTED");
            q.setRejectComment(comment != null ? comment : "Từ chối bởi Quản lý");
            quoteRepo.save(q);
            ra.addFlashAttribute("ok", "Đã từ chối báo giá.");
        }
        return "redirect:/dealer/quotes/pending";
    }
}

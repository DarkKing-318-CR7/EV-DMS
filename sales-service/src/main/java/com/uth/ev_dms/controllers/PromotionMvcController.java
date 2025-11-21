package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.service.PromotionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PromotionMvcController {

    private final PromotionService promotionService;
    private final UserRepo userRepo;

    public PromotionMvcController(PromotionService promotionService, UserRepo userRepo) {
        this.promotionService = promotionService;
        this.userRepo = userRepo;
    }

    // ================= STAFF VIEW =================
    @GetMapping("/staff/promotions")
    public String staffPromotions(Model model) {

        // Lấy username từ Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        com.uth.ev_dms.auth.User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy dealerId và region từ Dealer (KHÔNG dùng dealerBranch)
        Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;
        String region = (u.getDealer() != null) ? u.getDealer().getRegion() : null;

        // Lấy danh sách promotion hợp lệ
        List<Promotion> promos = promotionService.getValidPromotionsForQuote(dealerId, null, region);

        model.addAttribute("promotions", promos);
        model.addAttribute("readOnly", true);

        return "dealer/promotions";
    }

    // ================= MANAGER VIEW =================
    @GetMapping("/manager/promotions")
    public String managerPromotions(Model model) {

        // Lấy user đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        com.uth.ev_dms.auth.User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy dealer + region theo mô hình cũ
        Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;
        String region = (u.getDealer() != null) ? u.getDealer().getRegion() : null;

        // Lấy đúng promotion hợp lệ theo dealer + region
        List<Promotion> promos = promotionService.getValidPromotionsForQuote(dealerId, null, region);

        model.addAttribute("promotions", promos);
        model.addAttribute("readOnly", false);

        return "manager/promotions";
    }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.service.PromotionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PromotionMvcController {

    private final PromotionService promotionService;
    private final HttpServletRequest request;

    public PromotionMvcController(PromotionService promotionService,
                                  HttpServletRequest request) {
        this.promotionService = promotionService;
        this.request = request;
    }

    private Long getDealerId() {
        String v = request.getHeader("X-Dealer-Id");
        return v != null ? Long.valueOf(v) : null;
    }

    private String getRegion() {
        return request.getHeader("X-Region"); // Region được Gateway truyền qua
    }

    // ================= STAFF VIEW =================
    @GetMapping("/staff/promotions")
    public String staffPromotions(Model model) {

        Long dealerId = getDealerId();
        String region = getRegion();

        List<Promotion> promos =
                promotionService.getValidPromotionsForQuote(dealerId, null, region);

        model.addAttribute("promotions", promos);
        model.addAttribute("readOnly", true);

        return "dealer/promotions";
    }

    // ================= MANAGER VIEW =================
    @GetMapping("/manager/promotions")
    public String managerPromotions(Model model) {

        Long dealerId = getDealerId();
        String region = getRegion();

        List<Promotion> promos =
                promotionService.getValidPromotionsForQuote(dealerId, null, region);

        model.addAttribute("promotions", promos);
        model.addAttribute("readOnly", false);

        return "manager/promotions";
    }
}

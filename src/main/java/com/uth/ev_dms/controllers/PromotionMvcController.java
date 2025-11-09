package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.service.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class PromotionMvcController {

    private final PromotionService promotionService;

    public PromotionMvcController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // ✅ Trang dành cho Dealer Staff (xem & áp dụng khuyến mãi)
    @GetMapping("/staff/promotions")
    public String staffPromotions(Model model) {
        List<Promotion> promos = promotionService.getValidPromotions(null, null, null, LocalDate.now());
        model.addAttribute("promotions", promos);
        return "dealer/promotions"; // map tới file /templates/dealer/promotions.html
    }

    // ✅ Trang dành cho Manager (xem & duyệt khuyến mãi)
    @GetMapping("/manager/promotions")
    public String managerPromotions(Model model) {
        List<Promotion> promos = promotionService.findAll();
        model.addAttribute("promotions", promos);
        return "manager/promotions"; // map tới file /templates/manager/promotions.html
    }

}

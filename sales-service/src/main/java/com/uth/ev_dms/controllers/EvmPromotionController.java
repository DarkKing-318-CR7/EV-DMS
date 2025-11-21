package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.RegionRepo;
import com.uth.ev_dms.service.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/evm/promotions")
public class EvmPromotionController {

    private final PromotionService promotionService;
    private final RegionRepo regionRepo;

    // ⭐ Constructor injection đúng chuẩn
    public EvmPromotionController(PromotionService promotionService, RegionRepo regionRepo) {
        this.promotionService = promotionService;
        this.regionRepo = regionRepo;
    }

    @GetMapping
    public String listPromotions(Model model) {
        List<Promotion> promos = promotionService.getAllPromotions();
        model.addAttribute("promotions", promos);
        return "evm/orders/promotion";
    }

    @GetMapping("/new")
    public String newPromotionForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("regionsList", regionRepo.findAll());
        return "evm/orders/promotion-form";
    }

    @PostMapping("/save")
    public String savePromotion(@ModelAttribute Promotion promotion) {

        // Nếu chọn ALL thì chỉ lưu ALL duy nhất
        if (promotion.getRegions() != null && promotion.getRegions().contains("ALL")) {
            promotion.setRegions(List.of("ALL"));
        }

        promotionService.savePromotion(promotion);
        return "redirect:/evm/promotions";
    }

    @GetMapping("/edit/{id}")
    public String editPromotion(@PathVariable Long id, Model model) {
        Promotion promo = promotionService.getPromotionById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        model.addAttribute("promotion", promo);
        model.addAttribute("regionsList", regionRepo.findAll());
        return "evm/orders/promotion-form";
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return "redirect:/evm/promotions";
    }
}

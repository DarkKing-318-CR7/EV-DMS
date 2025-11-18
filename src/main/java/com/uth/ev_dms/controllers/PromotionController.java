package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.RegionRepo;
import com.uth.ev_dms.service.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final RegionRepo regionRepo;

    public PromotionController(PromotionService promotionService, RegionRepo regionRepo) {
        this.promotionService = promotionService;
        this.regionRepo = regionRepo;
    }

    @GetMapping
    public String listPromotions(Model model) {
        model.addAttribute("promotions", promotionService.getAllPromotions());
        return "admin/promotions/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("regionsList", regionRepo.findAll());
        return "admin/promotions/form";
    }

    @PostMapping("/save")
    public String savePromotion(@ModelAttribute Promotion promotion) {

        // Nếu chọn ALL → lưu duy nhất "ALL"
        if (promotion.getRegions() != null && promotion.getRegions().contains("ALL")) {
            promotion.setRegions(List.of("ALL"));
        }

        promotionService.savePromotion(promotion);
        return "redirect:/admin/promotions"; // hoặc /evm/promotions (tùy controller)
    }




    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Promotion promo = promotionService.getPromotionById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi ID: " + id));

        model.addAttribute("promotion", promo);
        model.addAttribute("regionsList", regionRepo.findAll());
        return "admin/promotions/form";
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return "redirect:/admin/promotions";
    }
}

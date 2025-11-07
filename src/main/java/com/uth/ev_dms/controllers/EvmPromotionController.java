package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.service.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/evm/promotions")
public class EvmPromotionController {

    private final PromotionService promotionService;

    public EvmPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // 📋 List tất cả khuyến mãi EVM
    @GetMapping
    public String listPromotions(Model model) {
        List<Promotion> promos = promotionService.getAllPromotions();
        model.addAttribute("promotions", promos);
        // templates/evm/orders/promotion.html
        return "evm/orders/promotion";
    }

    // ➕ Form tạo mới
    @GetMapping("/new")
    public String newPromotionForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        // templates/evm/orders/promotion-form.html
        return "evm/orders/promotion-form";
    }

    // 💾 Lưu (tạo mới / update)
    @PostMapping("/save")
    public String savePromotion(@ModelAttribute Promotion promotion) {
        promotionService.savePromotion(promotion);
        return "redirect:/evm/promotions";
    }

    // ✏️ Form sửa
    @GetMapping("/edit/{id}")
    public String editPromotion(@PathVariable Long id, Model model) {
        Promotion promo = promotionService.getPromotionById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        model.addAttribute("promotion", promo);
        return "evm/orders/promotion-form";
    }

    // 🗑️ Xóa
    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return "redirect:/evm/promotions";
    }
}

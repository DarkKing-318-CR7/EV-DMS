package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    @Autowired
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // 📋 Hiển thị danh sách khuyến mãi
    @GetMapping
    public String listPromotions(Model model) {
        model.addAttribute("promotions", promotionService.getAllPromotions());
        return "admin/promotions/list";
    }

    // ➕ Form thêm mới
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        return "admin/promotions/form";
    }

    // 💾 Lưu khuyến mãi mới
    @PostMapping
    public String savePromotion(@ModelAttribute Promotion promotion) {
        promotionService.savePromotion(promotion);
        return "redirect:/admin/promotions";
    }

    // 🖊️ Sửa khuyến mãi
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Promotion promotion = promotionService.getPromotionById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi ID: " + id));
        model.addAttribute("promotion", promotion);
        return "admin/promotions/form";
    }

    // 🗑️ Xóa khuyến mãi
    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return "redirect:/admin/promotions";
    }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.DealerBranch;
import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.DealerBranchRepo;
import com.uth.ev_dms.repo.RegionRepo;
import com.uth.ev_dms.service.PromotionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final DealerBranchRepo dealerBranchRepo;


    public PromotionController(PromotionService promotionService, DealerBranchRepo dealerBranchRepo) {
        this.promotionService = promotionService;
        this.dealerBranchRepo = dealerBranchRepo;
    }

    @GetMapping
    public String listPromotions(Model model) {
        model.addAttribute("promotions", promotionService.getAllPromotions());
        model.addAttribute("branchesList", dealerBranchRepo.findAll());
        return "admin/promotions/list";
    }


    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("branchesList", dealerBranchRepo.findAll());
        return "admin/promotions/form";
    }

    @PostMapping("/save")
    public String savePromotion(@ModelAttribute Promotion promotion) {

        List<Long> ids = promotion.getBranchIds();

        if (ids != null && ids.contains(-1L)) {
            List<Long> allBranchIds = dealerBranchRepo.findAll()
                    .stream()
                    .map(DealerBranch::getId)
                    .toList();
            promotion.setBranchIds(allBranchIds);
        }

        promotionService.savePromotion(promotion);
        return "redirect:/admin/promotions";
    }







    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Promotion promo = promotionService.getPromotionById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi ID: " + id));

        model.addAttribute("promotion", promo);
        model.addAttribute("branchesList", dealerBranchRepo.findAll());
        return "admin/promotions/form";
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return "redirect:/admin/promotions";
    }
}

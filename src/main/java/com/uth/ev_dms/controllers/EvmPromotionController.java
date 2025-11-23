package com.uth.ev_dms.controllers;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.DealerBranchRepo;
import com.uth.ev_dms.repo.RegionRepo;
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.service.PromotionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.uth.ev_dms.repo.UserRepo;

@Controller
@RequestMapping("/evm/promotions")
public class EvmPromotionController {

    private final PromotionService promotionService;
    private final DealerBranchRepo dealerBranchRepo;
    private final RegionRepo regionRepo;
    private final UserRepo userRepo;

    public EvmPromotionController(PromotionService promotionService,
                                  DealerBranchRepo dealerBranchRepo,
                                  RegionRepo regionRepo,
                                  UserRepo userRepo) {
        this.promotionService = promotionService;
        this.dealerBranchRepo = dealerBranchRepo;
        this.regionRepo = regionRepo;
        this.userRepo = userRepo;
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

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepo.findByUsername(username).orElseThrow();

// EVM tạo promotion cho dealer nào?
        promotion.setDealerId(u.getDealer().getId());


        // Nếu chọn ALL thì chỉ lưu ALL duy nhất
        // Nếu chọn ALL chi nhánh → set toàn bộ chi nhánh
        if (promotion.getBranchIds() != null && promotion.getBranchIds().contains(-1L)) {
            promotion.setBranchIds(
                    dealerBranchRepo.findAll().stream()
                            .map(b -> b.getId())
                            .toList()
            );
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

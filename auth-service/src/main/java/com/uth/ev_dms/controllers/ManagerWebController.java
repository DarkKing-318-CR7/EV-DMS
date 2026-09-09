package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.DealerRepo;
import com.uth.ev_dms.repo.PromotionRepo;
import com.uth.ev_dms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerWebController {

    private final PromotionRepo promotionRepo;
    private final DealerRepo dealerRepo;
    private final UserService userService;

    @GetMapping("/promotions")
    public String promotions(Model model) {
        List<Promotion> promotions = promotionRepo.findAll();
        model.addAttribute("promotions", promotions);
        model.addAttribute("active", "promotions");
        model.addAttribute("pageTitle", "Manager Promotions");
        return "dealer/promotions";
    }

    @GetMapping("/settings")
    public String settings(Model model, Principal principal) {
        Long dealerId = userService.getDealerId(principal);
        Dealer dealer = dealerId != null ? dealerRepo.findById(dealerId).orElse(null) : null;
        model.addAttribute("dealer", dealer);
        model.addAttribute("active", "settings");
        model.addAttribute("pageTitle", "Dealer Settings");
        return "redirect:/dealer/home";
    }
}

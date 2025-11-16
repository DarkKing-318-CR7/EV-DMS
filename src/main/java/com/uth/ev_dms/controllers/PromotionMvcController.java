    package com.uth.ev_dms.controllers;

    import com.uth.ev_dms.domain.Promotion;
    import com.uth.ev_dms.repo.UserRepo;
    import com.uth.ev_dms.service.PromotionService;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;

    import java.time.LocalDate;
    import java.util.List;

    @Controller
    public class PromotionMvcController {

        private final PromotionService promotionService;
        private final UserRepo userRepo;

        public PromotionMvcController(PromotionService promotionService, UserRepo userRepo) {
            this.promotionService = promotionService;
            this.userRepo = userRepo;
        }


        // ✅ Trang dành cho Dealer Staff (xem & áp dụng khuyến mãi)
        @GetMapping("/staff/promotions")
        public String staffPromotions(Model model) {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            com.uth.ev_dms.auth.User u = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;
            String region = (u.getDealerBranch() != null && u.getDealerBranch().getDealer() != null)
                    ? u.getDealerBranch().getDealer().getRegion()
                    : null;

            List<Promotion> promos = promotionService.getValidPromotionsForQuote(dealerId, null, region);

            model.addAttribute("promotions", promos);
            model.addAttribute("readOnly", true);

            return "dealer/promotions";
        }


        // ✅ Trang dành cho Manager (xem & duyệt khuyến mãi)
        @GetMapping("/manager/promotions")
        public String managerPromotions(Model model) {

            // Lấy user đang đăng nhập
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            com.uth.ev_dms.auth.User u = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Xác định dealerId & region theo user
            Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;
            String region = (u.getDealerBranch() != null && u.getDealerBranch().getDealer() != null)
                    ? u.getDealerBranch().getDealer().getRegion()
                    : null;

            // 🔥 Lấy đúng danh sách promotion theo dealer & region
            List<Promotion> promos = promotionService.getValidPromotionsForQuote(dealerId, null, region);

            model.addAttribute("promotions", promos);
            model.addAttribute("readOnly", false); // manager vẫn có quyền

            return "manager/promotions";
        }


    }

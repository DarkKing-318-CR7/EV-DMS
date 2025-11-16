package com.uth.ev_dms.controllers;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.PriceList;
import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.repo.PriceListRepo;
import com.uth.ev_dms.repo.TrimRepo;
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.service.PromotionService;
import com.uth.ev_dms.service.SalesService;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dealer/quotes")
public class DealerQuoteController {

    private final SalesService salesService;
    private final PromotionService promotionService;
    private final VehicleRepo vehicleRepo;
    private final PriceListRepo priceListRepo;
    private final TrimRepo trimRepo;
    private final UserRepo userRepo;

    public DealerQuoteController(SalesService salesService,
                                 PromotionService promotionService,
                                 VehicleRepo vehicleRepo,
                                 PriceListRepo priceListRepo,
                                 TrimRepo trimRepo,
                                 UserRepo userRepo) {

        this.salesService = salesService;
        this.promotionService = promotionService;
        this.vehicleRepo = vehicleRepo;
        this.priceListRepo = priceListRepo;
        this.trimRepo = trimRepo;
        this.userRepo = userRepo;
    }

    // ================= STAFF =================
    @GetMapping("/my")
    public String myQuotes(Model model) {
        List<Quote> quotes = salesService.findAll();
        model.addAttribute("quotes", quotes);
        model.addAttribute("role", "STAFF");
        return "dealer/quotes";
    }

    // ================= FORM TẠO QUOTE =================
    @GetMapping("/my/new")
    public String createForm(Model model) {
        model.addAttribute("quote", new CreateQuoteDTO());

        // Lấy username từ Security
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Lấy toàn bộ user từ DB để truy xuất dealer + region
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;
        String region = (u.getDealerBranch() != null && u.getDealerBranch().getDealer() != null)
                ? u.getDealerBranch().getDealer().getRegion()
                : null;

        // Debug nếu cần
        System.out.println(">>> LOAD PROMOS FOR DealerId=" + dealerId + " | Region=" + region);

        // 🎯 Chỉ load các promotion áp dụng đúng branch/dealer/region cho staff
        List<Promotion> promos = promotionService.getValidPromotionsForQuote(dealerId, null, region);
        model.addAttribute("promotions", promos);

        // Vehicles + Price
        List<Map<String, Object>> trimsWithPrice = trimRepo.findAllActiveWithPrice().stream()
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("trimId", t.getId());
                    m.put("trimName", t.getTrimName());
                    m.put("vehicleName", t.getVehicle().getModelName());
                    m.put("price", t.getCurrentPrice());
                    return m;
                })
                .toList();

        model.addAttribute("vehicles", trimsWithPrice);

        return "dealer/quote-create";
    }

    // ================= LƯU QUOTE =================
    @PostMapping("/my/save")
    public String saveQuote(@ModelAttribute CreateQuoteDTO dto,
                            @RequestParam(value = "promotionIds", required = false) List<Long> promotionIds) {

        Quote q = salesService.createQuote(dto);
        System.out.println("💾 Quote vừa tạo ID = " + q.getId() + ", total = " + q.getTotalAmount());

        if (promotionIds != null && !promotionIds.isEmpty()) {
            Quote updated = salesService.applyPromotions(q.getId(), promotionIds);
            System.out.println("✅ Apply promotions: " + promotionIds +
                    " -> discount = " + updated.getAppliedDiscount() +
                    ", final = " + updated.getFinalAmount());
        } else {
            System.out.println("⚠️ No promotions selected");
        }

        return "redirect:/dealer/quotes/my";
    }

    // ================= SUBMIT QUOTE =================
    @PostMapping("/submit/{id}")
    public String submitQuote(@PathVariable Long id) {
        salesService.submitQuote(id);
        return "redirect:/dealer/quotes/my";
    }

    // ================= MANAGER =================
    @GetMapping("/pending")
    public String pending(Model model) {
        model.addAttribute("quotes", salesService.findPending());
        model.addAttribute("role", "MANAGER");
        return "dealer/quotes";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        salesService.approveQuote(id);
        return "redirect:/dealer/quotes/pending";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, @RequestParam String comment) {
        salesService.rejectQuote(id, comment);
        return "redirect:/dealer/quotes/pending";
    }

// ================= AJAX API =================

    // Lấy danh sách trims kèm giá để dùng trong dropdown
    @GetMapping("/api/trims")
    @ResponseBody
    public List<Map<String, Object>> getAllTrims() {
        return trimRepo.findAllActiveWithPrice().stream()
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("trimId", t.getId());
                    m.put("trimName", t.getTrimName());
                    m.put("vehicleName", t.getVehicle().getModelName());
                    m.put("price", t.getCurrentPrice());
                    return m;
                })
                .toList();
    }

    // Lấy giá theo trimId
    @GetMapping("/api/price/{trimId}")
    @ResponseBody
    public BigDecimal getPriceByTrim(@PathVariable Long trimId) {
        return trimRepo.findById(trimId)
                .map(Trim::getCurrentPrice)
                .orElse(BigDecimal.ZERO);
    }


    @GetMapping("/api/promotions")
    @ResponseBody
    public List<Map<String, Object>> getPromotionsApi() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepo.findByUsername(username).orElseThrow();

        Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;
        String region  = (u.getDealerBranch() != null && u.getDealerBranch().getDealer() != null)
                ? u.getDealerBranch().getDealer().getRegion()
                : null;

        List<Promotion> list = promotionService.getValidPromotionsForQuote(dealerId, null, region);

        return list.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());

            // Lấy tên hiển thị ưu tiên theo thứ tự
            if (p.getTitle() != null && !p.getTitle().isEmpty())
                m.put("name", p.getTitle());
            else if (p.getName() != null && !p.getName().isEmpty())
                m.put("name", p.getName());
            else if (p.getDescription() != null && !p.getDescription().isEmpty())
                m.put("name", p.getDescription());
            else
                m.put("name", "(Unnamed promotion)");

            // giá trị giảm: ưu tiên discountPercent → discountRate
            if (p.getDiscountPercent() != null)
                m.put("discount", p.getDiscountPercent() + "%");
            else if (p.getDiscountRate() != null)
                m.put("discount", p.getDiscountRate());
            else
                m.put("discount", "N/A");

            return m;
        }).toList();
    }




}
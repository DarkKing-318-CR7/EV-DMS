package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.PriceList;
import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.repo.PriceListRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.service.PromotionService;
import com.uth.ev_dms.service.SalesService;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.uth.ev_dms.repo.TrimRepo;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


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


    // ✅ Constructor đầy đủ dependencies
    public DealerQuoteController(SalesService salesService,
                                 PromotionService promotionService,
                                 VehicleRepo vehicleRepo,
                                 PriceListRepo priceListRepo,
                                 TrimRepo trimRepo) {
        this.salesService = salesService;
        this.promotionService = promotionService;
        this.vehicleRepo = vehicleRepo;
        this.priceListRepo = priceListRepo;
        this.trimRepo = trimRepo;
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

        List<Promotion> promos = promotionService.getValidPromotions(null, null, null, LocalDate.now());
        model.addAttribute("promotions", promos);

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

        model.addAttribute("vehicles", trimsWithPrice); // giữ nguyên key 'vehicles' để frontend không phải sửa HTML

        return "dealer/quote-create";
    }

    // ================= LƯU QUOTE =================
    @PostMapping("/my/save")
    public String saveQuote(@ModelAttribute CreateQuoteDTO dto,
                            @RequestParam(value = "promotionIds", required = false) List<Long> promotionIds) {

        // 1️⃣ Tạo quote trước
        Quote q = salesService.createQuote(dto);
        System.out.println("💾 Quote vừa tạo ID = " + q.getId() + ", total = " + q.getTotalAmount());

        // 2️⃣ Áp dụng khuyến mãi nếu có
        if (promotionIds != null && !promotionIds.isEmpty()) {
            Quote updated = salesService.applyPromotions(q.getId(), promotionIds);
            System.out.println("✅ Áp dụng promotions: " + promotionIds +
                    " -> discount = " + updated.getAppliedDiscount() +
                    ", final = " + updated.getFinalAmount());
        } else {
            System.out.println("⚠️ Không có promotions được chọn!");
        }

        // 3️⃣ Quay lại trang danh sách
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

    // ================== AJAX: GET PRICE BY MODEL CODE ==================
//    @GetMapping("/price/{modelCode}")
//    @ResponseBody
//    public BigDecimal getPriceByModelCode(@PathVariable String modelCode) {
//        return priceListRepo.findActiveByModelCodeAtDate(modelCode, LocalDate.now())
//                .map(PriceList::getMsrp)
//                .orElse(BigDecimal.ZERO);
//    }

    // ================== API: GET VEHICLES (cho JS fetch) ==================
    @GetMapping("/api/vehicles")
    @ResponseBody
    public List<Map<String, Object>> getVehiclesApi() {
        return vehicleRepo.findAll().stream()
                .map(v -> {
                    BigDecimal price = priceListRepo
                            .findActiveByModelCodeAtDate(v.getModelCode(), LocalDate.now())
                            .map(PriceList::getMsrp)
                            .orElse(BigDecimal.ZERO);

                    Map<String, Object> m = new HashMap<>();
                    m.put("id", v.getId());
                    m.put("brand", v.getBrand());
                    m.put("modelName", v.getModelName());
                    m.put("modelCode", v.getModelCode());
                    m.put("price", price);
                    return m;
                })
                .toList();
    }


    // ⚙️ Lấy giá xe theo TrimId (đúng cho form Quote)
    @GetMapping("/price/{trimId}")
    @ResponseBody
    public BigDecimal getTrimPrice(@PathVariable Long trimId) {
        return trimRepo.findById(trimId)
                .map(t -> t.getCurrentPrice())
                .orElse(BigDecimal.ZERO);
    }



}

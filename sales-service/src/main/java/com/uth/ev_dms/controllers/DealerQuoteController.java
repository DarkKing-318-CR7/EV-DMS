package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.repo.PriceListRepo;
import com.uth.ev_dms.repo.QuoteRepo;
import com.uth.ev_dms.repo.TrimRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.repo.InventoryRepo;
import com.uth.ev_dms.service.PromotionService;
import com.uth.ev_dms.service.SalesService;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final QuoteRepo quoteRepo;
    private final InventoryRepo inventoryRepo;

    public DealerQuoteController(SalesService salesService,
                                 PromotionService promotionService,
                                 VehicleRepo vehicleRepo,
                                 PriceListRepo priceListRepo,
                                 TrimRepo trimRepo,
                                 QuoteRepo quoteRepo,
                                 InventoryRepo inventoryRepo) {

        this.salesService = salesService;
        this.promotionService = promotionService;
        this.vehicleRepo = vehicleRepo;
        this.priceListRepo = priceListRepo;
        this.trimRepo = trimRepo;
        this.quoteRepo = quoteRepo;
        this.inventoryRepo = inventoryRepo;
    }


    // ================= STAFF: LIST QUOTE CỦA ĐẠI LÝ MÌNH =================
    @GetMapping("/my")
    public String myQuotes(Model model, HttpServletRequest request) {

        Long dealerId = Long.valueOf(request.getHeader("X-DEALER-ID"));
        String role = request.getHeader("X-ROLE");

        List<Quote> quotes = quoteRepo.findByDealerId(dealerId);

        model.addAttribute("quotes", quotes);
        model.addAttribute("role", role);
        return "dealer/quotes";
    }


    // ================= FORM TẠO QUOTE =================
    @GetMapping("/my/new")
    public String createForm(Model model, HttpServletRequest request) {

        model.addAttribute("quote", new CreateQuoteDTO());

        Long dealerId = Long.valueOf(request.getHeader("X-DEALER-ID"));
        String region = request.getHeader("X-REGION");

        List<Promotion> promos =
                promotionService.getValidPromotionsForQuote(dealerId, null, region);
        model.addAttribute("promotions", promos);

        List<Map<String, Object>> trimsWithPrice = trimRepo.findAllActiveWithPrice()
                .stream()
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
    public String saveQuote(@ModelAttribute("quote") CreateQuoteDTO dto,
                            BindingResult result,
                            Model model,
                            HttpServletRequest request,
                            @RequestParam(value = "promotionIds", required = false) List<Long> promotionIds) {

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return reloadFormWithError(model, "You must add at least one vehicle to quote.", request);
        }

        try {
            Quote q = salesService.createQuote(dto);

            if (promotionIds != null && !promotionIds.isEmpty()) {
                q = salesService.applyPromotions(q.getId(), promotionIds);
            }

            return "redirect:/dealer/quotes/my";
        } catch (Exception ex) {
            return reloadFormWithError(model, ex.getMessage(), request);
        }
    }


    private String reloadFormWithError(Model model, String message, HttpServletRequest request) {
        model.addAttribute("errorMessage", message);

        Long dealerId = Long.valueOf(request.getHeader("X-DEALER-ID"));
        String region = request.getHeader("X-REGION");

        List<Promotion> promos = promotionService.getValidPromotionsForQuote(dealerId, null, region);
        model.addAttribute("promotions", promos);

        List<Map<String, Object>> trimsWithPrice = trimRepo.findAllActiveWithPrice()
                .stream()
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("trimId", t.getId());
                    m.put("trimName", t.getTrimName());
                    m.put("vehicleName", t.getVehicle().getModelName());
                    m.put("price", t.getCurrentPrice());
                    return m;
                }).toList();

        model.addAttribute("vehicles", trimsWithPrice);

        return "dealer/quote-create";
    }


    // ================= SUBMIT QUOTE =================
    @PostMapping("/submit/{id}")
    public String submitQuote(@PathVariable Long id) {
        salesService.submitQuote(id);
        return "redirect:/dealer/quotes/my";
    }


    // ================= MANAGER: XEM QUOTE PENDING CỦA ĐẠI LÝ =================
    @GetMapping("/pending")
    public String pending(Model model, HttpServletRequest request) {

        Long dealerId = Long.valueOf(request.getHeader("X-DEALER-ID"));

        List<Quote> quotes = quoteRepo.findByDealerIdAndStatus(dealerId, "PENDING");

        model.addAttribute("quotes", quotes);
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


    // ================= API TRIMS =================
    @GetMapping("/api/trims")
    @ResponseBody
    public List<Map<String, Object>> getAllTrims(HttpServletRequest request) {

        Long dealerId = Long.valueOf(request.getHeader("X-DEALER-ID"));

        List<Trim> trims;
        if (dealerId != null) {
            trims = inventoryRepo.findAvailableTrimsByDealer(dealerId);
        } else {
            trims = trimRepo.findAllActiveWithPrice();
        }

        return trims.stream()
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


    @GetMapping("/api/price/{trimId}")
    @ResponseBody
    public BigDecimal getPriceByTrim(@PathVariable Long trimId) {
        return trimRepo.findById(trimId)
                .map(Trim::getCurrentPrice)
                .orElse(BigDecimal.ZERO);
    }


    @GetMapping("/api/promotions")
    @ResponseBody
    public List<Map<String, Object>> getPromotionsApi(HttpServletRequest request) {

        Long dealerId = Long.valueOf(request.getHeader("X-DEALER-ID"));
        String region = request.getHeader("X-REGION");

        List<Promotion> list =
                promotionService.getValidPromotionsForQuote(dealerId, null, region);

        return list.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());

            String name =
                    (p.getTitle() != null && !p.getTitle().isEmpty()) ? p.getTitle() :
                            (p.getName() != null && !p.getName().isEmpty()) ? p.getName() :
                                    (p.getDescription() != null) ? p.getDescription() :
                                            "Unnamed";

            m.put("name", name);

            if (p.getDiscountPercent() != null)
                m.put("discount", p.getDiscountPercent());
            else if (p.getDiscountRate() != null)
                m.put("discount", p.getDiscountRate());
            else
                m.put("discount", "N/A");

            return m;
        }).toList();
    }
}

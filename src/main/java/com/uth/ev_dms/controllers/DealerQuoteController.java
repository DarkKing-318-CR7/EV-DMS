package com.uth.ev_dms.controllers;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.repo.PriceListRepo;
import com.uth.ev_dms.repo.QuoteRepo;
import com.uth.ev_dms.repo.TrimRepo;
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.service.PromotionService;
import com.uth.ev_dms.service.SalesService;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.uth.ev_dms.repo.InventoryRepo;
import com.uth.ev_dms.domain.Trim;

@Controller
@RequestMapping("/dealer/quotes")
public class DealerQuoteController {

    private final SalesService salesService;
    private final PromotionService promotionService;
    private final VehicleRepo vehicleRepo;
    private final PriceListRepo priceListRepo;
    private final TrimRepo trimRepo;
    private final UserRepo userRepo;
    private final QuoteRepo quoteRepo;   // 👈 thêm repo để filter theo dealer
    private final InventoryRepo inventoryRepo;

    public DealerQuoteController(SalesService salesService,
                                 PromotionService promotionService,
                                 VehicleRepo vehicleRepo,
                                 PriceListRepo priceListRepo,
                                 TrimRepo trimRepo,
                                 UserRepo userRepo,
                                 QuoteRepo quoteRepo,
                                 InventoryRepo inventoryRepo) {

        this.salesService = salesService;
        this.promotionService = promotionService;
        this.vehicleRepo = vehicleRepo;
        this.priceListRepo = priceListRepo;
        this.trimRepo = trimRepo;
        this.userRepo = userRepo;
        this.quoteRepo = quoteRepo;
        this.inventoryRepo = inventoryRepo;
    }

    // ================= STAFF: LIST QUOTE CỦA ĐẠI LÝ MÌNH =================
    @GetMapping("/my")
    public String myQuotes(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepo.findByUsername(username).orElseThrow();

        Long dealerId = u.getDealer() != null ? u.getDealer().getId() : null;

        // Chỉ lấy quote của đúng dealer
        List<Quote> quotes = quoteRepo.findByDealerId(dealerId);

        model.addAttribute("quotes", quotes);
        model.addAttribute("role", "STAFF");
        return "dealer/quotes";
    }


    // ================= FORM TẠO QUOTE =================
    @GetMapping("/my/new")
    public String createForm(Model model) {
        model.addAttribute("quote", new CreateQuoteDTO());

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;
        String region  = (u.getDealer() != null) ? u.getDealer().getRegion() : null;

        // khuyến mãi hợp lệ cho dealer + region hiện tại
        List<Promotion> promos =
                promotionService.getValidPromotionsForQuote(dealerId, null, region);
        model.addAttribute("promotions", promos);

        // danh sách trims + price
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
                            @RequestParam(value = "promotionIds", required = false) List<Long> promotionIds) {

        // validate must have item
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return reloadFormWithError(model, "You must add at least one vehicle to quote.");
        }

        try {
            // normal save
            Quote q = salesService.createQuote(dto);

            if (promotionIds != null && !promotionIds.isEmpty()) {
                q = salesService.applyPromotions(q.getId(), promotionIds);
            }

            return "redirect:/dealer/quotes/my";
        }
        catch (Exception ex) {
            return reloadFormWithError(model, ex.getMessage());
        }
    }


    private String reloadFormWithError(Model model, String message) {
        model.addAttribute("errorMessage", message);

        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepo.findByUsername(username).orElseThrow();
        Long dealerId = u.getDealer().getId();
        String region = u.getDealer().getRegion();

        // Load promotions again
        List<Promotion> promos = promotionService.getValidPromotionsForQuote(dealerId, null, region);
        model.addAttribute("promotions", promos);

        // Load vehicles again
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

        // Return back to form
        return "dealer/quote-create";
    }


    // ================= SUBMIT QUOTE =================
    @PostMapping("/submit/{id}")
    public String submitQuote(@PathVariable Long id) {
        salesService.submitQuote(id);
        return "redirect:/dealer/quotes/my";
    }

    // ================= MANAGER: XEM QUOTE PENDING CỦA ĐẠI LÝ MÌNH =================
    @GetMapping("/pending")
    public String pending(Model model) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User u = userRepo.findByUsername(username).orElseThrow();

        Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;

        // chỉ lấy quote PENDING thuộc dealer của user
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

    // ================== API PHỤ TRỢ ==================
    @GetMapping("/api/trims")
    @ResponseBody
    public List<Map<String, Object>> getAllTrims() {

        // Lấy user hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;

        // Nếu user thuộc 1 dealer -> chỉ lấy các trim còn hàng của dealer đó
        List<Trim> trims;
        if (dealerId != null) {
            trims = inventoryRepo.findAvailableTrimsByDealer(dealerId);
        } else {
            // fallback: nếu vì lý do gì user không gắn dealer, lấy như cũ
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
    public List<Map<String, Object>> getPromotionsApi() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User u = userRepo.findByUsername(username).orElseThrow();

        Long dealerId = (u.getDealer() != null) ? u.getDealer().getId() : null;
        String region  = (u.getDealer() != null) ? u.getDealer().getRegion() : null;

        List<Promotion> list =
                promotionService.getValidPromotionsForQuote(dealerId, null, region);

        return list.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());

            String name =
                    (p.getTitle() != null && !p.getTitle().isEmpty()) ? p.getTitle() :
                            (p.getName() != null && !p.getName().isEmpty())   ? p.getName() :
                                    (p.getDescription() != null)                      ? p.getDescription() :
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

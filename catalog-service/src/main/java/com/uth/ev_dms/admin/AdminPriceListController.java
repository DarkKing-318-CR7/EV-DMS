package com.uth.ev_dms.admin;

import com.uth.ev_dms.domain.PriceList;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPriceListController {

    private final ProductService productService;

    // LIST: /admin/trims/{trimId}/prices
    @GetMapping("/trims/{trimId}/prices")
    public String list(@PathVariable Long trimId, Model model) {
        Trim trim = productService.getTrimOrThrow(trimId);
        model.addAttribute("trim", trim);
        model.addAttribute("prices", productService.listPricesByTrim(trimId));
        return "admin/prices/list";   // view: templates/admin/prices/list.html
    }

    // FORM CREATE: /admin/trims/{trimId}/prices/create
    @GetMapping("/trims/{trimId}/prices/create")
    public String createForm(@PathVariable Long trimId, Model model) {
        var trim = productService.getTrimOrThrow(trimId);
        var price = new PriceList();
        price.setTrim(trim);
        model.addAttribute("price", price);
        return "admin/prices/form";   // view: templates/admin/prices/form.html
    }

    // SAVE: POST /admin/prices  (CHÚ Ý: chuỗi thuần, không dùng @{} ở đây)
    @PostMapping("/prices")
    public String save(@ModelAttribute("price") PriceList price) {
        PriceList saved = productService.savePrice(price);
        return "redirect:/admin/trims/" + saved.getTrim().getId() + "/prices";
    }

    // OPTIONAL: deactivate
    @PostMapping("/prices/{id}/deactivate")
    public String deactivate(@PathVariable Long id, @RequestParam Long trimId) {
        productService.deactivatePrice(id);
        return "redirect:/admin/trims/" + trimId + "/prices";
    }
}

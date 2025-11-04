package com.uth.ev_dms.controllers;
//
import com.uth.ev_dms.service.ProductService;
import com.uth.ev_dms.service.dto.TrimCommercialForm;
import com.uth.ev_dms.service.dto.TrimPricingForm;
import com.uth.ev_dms.service.dto.VehicleCommercialForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/evm/products")
public class EvmProductController {

    private final ProductService productService;

    @GetMapping
    public String list(Model model) {

        var vehicles = productService.getVehiclesWithTrims();
        System.out.println("[EVM PRODUCTS] vehicles size = "
                + (vehicles == null ? "null" : vehicles.size()));

        model.addAttribute("vehicles", vehicles);

        model.addAttribute("active", "products");
        model.addAttribute("pageTitle", "Products - EVM Staff View");

        model.addAttribute("canCreateProducts", false);     // EVM không tạo model/trim
        model.addAttribute("canMaintainPricing", true);     // EVM được chỉnh giá/trạng thái

        return "evm/products/list";
    }


    @GetMapping("/vehicle/{id}/edit")
    public String editVehicleForm(@PathVariable Long id, Model model) {
        // 1. Lấy data gốc từ service
        VehicleCommercialForm form = productService.getVehicleCommercialForm(id);

        // 2. Setup common attrs
        model.addAttribute("form", form);
        model.addAttribute("pageTitle", "Edit Vehicle");
        model.addAttribute("active", "products");

        return "evm/products/vehicle-edit";
    }

    @PostMapping("/vehicle/{id}/edit")
    public String updateVehicle(
            @PathVariable Long id,
            @ModelAttribute("form") VehicleCommercialForm form
    ) {
        productService.updateVehicleCommercialInfo(id, form);
        return "redirect:/evm/products"; // quay về list
    }

    @GetMapping("/trim/{trimId}/edit")
    public String editTrimForm(@PathVariable Long trimId, Model model) {
        TrimCommercialForm form = productService.getTrimCommercialForm(trimId);

        model.addAttribute("form", form);
        model.addAttribute("pageTitle", "Edit Trim");
        model.addAttribute("active", "products");

        return "evm/products/trim-edit";
    }

    @PostMapping("/trim/{trimId}/edit")
    public String updateTrim(
            @PathVariable Long trimId,
            @ModelAttribute("form") TrimCommercialForm form
    ) {
        productService.updateTrimCommercialInfo(trimId, form);
        return "redirect:/evm/products";
    }

    @GetMapping("/trim/{trimId}/price")
    public String editPriceForm(@PathVariable Long trimId, Model model) {
        TrimPricingForm form = productService.getTrimPricingForm(trimId);

        model.addAttribute("form", form);
        model.addAttribute("pageTitle", "Update Price");
        model.addAttribute("active", "products");

        return "evm/products/price-edit";
    }

    @PostMapping("/trim/{trimId}/price")
    public String updatePrice(
            @PathVariable Long trimId,
            @ModelAttribute("form") TrimPricingForm form
    ) {
        productService.updateTrimPricing(trimId, form);
        return "redirect:/evm/products";
    }



}
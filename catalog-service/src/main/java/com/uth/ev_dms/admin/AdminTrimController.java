package com.uth.ev_dms.admin;



import com.uth.ev_dms.domain.DriveType;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminTrimController {

    private final ProductService productService;

    // ===== CREATE =====
    @GetMapping("/products/{vehicleId}/trims/create")
    public String createForm(@PathVariable Long vehicleId, Model model) {
        var v = productService.getVehicleOrThrow(vehicleId);
        var t = new Trim();
        t.setVehicle(v);
        model.addAttribute("trim", t);
        model.addAttribute("driveTypes", DriveType.values());
        model.addAttribute("formAction", "/admin/trims");
        model.addAttribute("formTitle", "Create Trim");
        model.addAttribute("isEdit", false);
        return "admin/trims/form";
    }

    @PostMapping("/trims")
    public String create(@ModelAttribute("trim") Trim trim) {
        var saved = productService.saveTrim(trim);
        return "redirect:/admin/products/" + saved.getVehicle().getId();
    }

    // ===== VIEW =====
    @GetMapping("/trims/{id}")
    public String view(@PathVariable Long id, Model model) {
        var trim = productService.getTrimOrThrow(id);
        model.addAttribute("trim", trim);
        return "admin/trims/view";
    }

    // ===== EDIT =====
    @GetMapping("/trims/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var trim = productService.getTrimOrThrow(id);
        model.addAttribute("trim", trim);
        model.addAttribute("driveTypes", DriveType.values());
        model.addAttribute("formAction", "/admin/trims/" + trim.getId());
        model.addAttribute("formTitle", "Edit Trim");
        model.addAttribute("isEdit", true);
        model.addAttribute("hasPrices", productService.hasPricesForTrim(id));
        return "admin/trims/form";
    }

    @PostMapping("/trims/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("trim") Trim trim) {
        trim.setId(id);
        var saved = productService.saveTrim(trim);
        return "redirect:/admin/trims/" + saved.getId();
    }

    // ===== DELETE =====
    @PostMapping("/trims/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam Long vehicleId) {
        try {
            productService.deleteTrim(id);
            return "redirect:/admin/products/" + vehicleId + "?msg=trim_deleted";
        } catch (IllegalStateException ex) {
            return "redirect:/admin/trims/" + id + "/edit?error=has_prices";
        }
    }
}

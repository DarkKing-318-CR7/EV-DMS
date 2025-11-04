package com.uth.ev_dms.admin;

import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    // ===== LIST =====
    @GetMapping({"", "/"})
    public String list(Model model) {
        model.addAttribute("vehicles", productService.listVehicles());
        model.addAttribute("active", "products");
        model.addAttribute("pageTitle", "Products");
        return "admin/products/list";
    }

    // ===== CREATE (FORM) =====
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("formTitle", "Create Vehicle");
        model.addAttribute("formAction", "/admin/products"); // Đường dẫn post
        return "admin/products/form";
    }

    // ===== CREATE (SUBMIT) =====
    @PostMapping
    public String createSubmit(@Valid @ModelAttribute("vehicle") Vehicle v,
                               BindingResult br,
                               Model model,
                               RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("active", "products");
            model.addAttribute("pageTitle", "Create Vehicle");
            return "admin/products/form";
        }
        Vehicle saved = productService.saveVehicle(v);
        ra.addFlashAttribute("success", "Vehicle created successfully");
        return "redirect:/admin/products/" + saved.getId();
    }

    // ===== DETAIL =====
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Vehicle v = productService.getVehicleOrThrow(id);
        model.addAttribute("vehicle", v);
        model.addAttribute("trims", productService.listTrimsByVehicle(id));
        model.addAttribute("active", "products");
        model.addAttribute("pageTitle", "Vehicle Detail");
        return "admin/products/detail";
    }

    // ===== EDIT (FORM) =====
    @GetMapping("/{id}/edit")
    public String editVehicleForm(@PathVariable Long id, Model model){
        var v = productService.getVehicleOrThrow(id);
        model.addAttribute("vehicle", v);
        model.addAttribute("formTitle", "Edit Vehicle");
        model.addAttribute("isEdit", true); // <-- quan trọng
        return "admin/products/form";
    }


    // ===== UPDATE (SUBMIT) =====
    @PostMapping("/{id}")
    public String updateVehicle(@PathVariable Long id,
                                @Valid @ModelAttribute("vehicle") Vehicle vehicle,
                                BindingResult br,
                                Model model,
                                RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("active", "products");
            model.addAttribute("pageTitle", "Edit Vehicle");
            return "admin/products/form";
        }
        vehicle.setId(id);
        productService.saveVehicle(vehicle);
        ra.addFlashAttribute("success", "Vehicle updated successfully");
        return "redirect:/admin/products/" + id;
    }
    @PostMapping("/{id}/delete")
    public String deleteVehicle(@PathVariable Long id) {
        productService.deleteVehicle(id);
        return "redirect:/admin/products";
    }
}

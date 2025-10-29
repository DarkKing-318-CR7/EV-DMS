package com.uth.ev_dms.admin;

import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    // ====== HIỂN THỊ DANH SÁCH XE ======
    @GetMapping({"", "/"})
    public String list(Model model) {
        model.addAttribute("vehicles", productService.listVehicles());
        return "admin/products/list";
    }

    // ====== FORM TẠO MỚI ======
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        return "admin/products/form";
    }

    // ====== XỬ LÝ LƯU SAU KHI BẤM SAVE ======
    @PostMapping
    public String save(@ModelAttribute("vehicle") Vehicle vehicle) {
        productService.saveVehicle(vehicle);
        return "redirect:/admin/products";   // <-- sau khi lưu, về trang danh sách
    }

    // ====== XEM CHI TIẾT MỘT XE ======
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Vehicle v = productService.getVehicle(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vehicle id " + id));
        model.addAttribute("vehicle", v);
        model.addAttribute("trims", productService.listTrimsByVehicle(id));
        model.addAttribute("productService", productService);
        return "admin/products/detail";
    }

    // ========== EDIT FORM ==========
    @GetMapping("/{id}/edit")
    public String editVehicleForm(@PathVariable Long id, Model model){
        var v = productService.getVehicleOrThrow(id);
        model.addAttribute("vehicle", v);
        model.addAttribute("formTitle", "Edit Vehicle");
        model.addAttribute("isEdit", true);
        return "admin/products/form";  // dùng chung form create/edit
    }

    // ========== UPDATE ==========
    @PostMapping("/{id}")
    public String updateVehicle(@PathVariable Long id, @ModelAttribute("vehicle") Vehicle vehicle){
        vehicle.setId(id);                     // đảm bảo đúng id
        productService.saveVehicle(vehicle);   // service sẽ validate
        return "redirect:/admin/products/" + id;
    }
}

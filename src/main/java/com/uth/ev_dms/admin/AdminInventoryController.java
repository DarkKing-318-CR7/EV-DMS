package com.uth.ev_dms.admin;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.service.InventoryService;
import com.uth.ev_dms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService; // để lấy list trims hiển thị dropdown

    @GetMapping({"", "/"})
    public String list(Model model) {
        model.addAttribute("inventories", inventoryService.listAll());
        model.addAttribute("active", "inventory");
        return "admin/inventory/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        Inventory inv = new Inventory();
        inv.setQtyOnHand(0);
        inv.setLocationType("EVM");

        model.addAttribute("inventory", inv);
        model.addAttribute("allTrims", productService.listAllTrims());
        model.addAttribute("isEdit", false);
        model.addAttribute("active", "inventory");
        return "admin/inventory/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Inventory inv = inventoryService.getOrThrow(id);

        model.addAttribute("inventory", inv);
        model.addAttribute("allTrims", productService.listAllTrims());
        model.addAttribute("isEdit", true);
        model.addAttribute("active", "inventory");
        return "admin/inventory/form";
    }

    @PostMapping
    public String save(@ModelAttribute("inventory") Inventory inv) {
        inventoryService.save(inv);
        return "redirect:/admin/inventory";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return "redirect:/admin/inventory";
    }
}

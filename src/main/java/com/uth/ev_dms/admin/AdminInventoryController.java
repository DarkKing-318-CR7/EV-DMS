package com.uth.ev_dms.admin;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
import com.uth.ev_dms.service.InventoryService;
import com.uth.ev_dms.service.ProductService;
import com.uth.ev_dms.service.DealerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final DealerService dealerService;

    // 1. Danh sách tồn kho
    @GetMapping
    public String list(
            @RequestParam(value = "success", required = false) String successFlag,
            Model model
    ) {
        model.addAttribute("inventories", inventoryService.findAll());
        model.addAttribute("success", successFlag != null);
        model.addAttribute("active", "inventory");
        model.addAttribute("pageTitle", "Inventory");
        return "admin/inventory/list";
    }

    // 2. Form tạo mới
    @GetMapping("/create")
    public String createForm(Model model) {

        Inventory blank = new Inventory();
        blank.setLocationType("EVM");
        blank.setQtyOnHand(0);

        List<Trim> allTrims = productService.getAllTrims();
        List<Dealer> allDealers = dealerService.getAllDealers();

        model.addAttribute("inventory", blank);
        model.addAttribute("allTrims", allTrims);
        model.addAttribute("allDealers", allDealers);
        model.addAttribute("isEdit", false);
        model.addAttribute("active", "inventory");
        model.addAttribute("pageTitle", "Create Inventory");

        return "admin/inventory/form";
    }

    // 3. Form edit
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {

        Inventory inv = inventoryService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + id));

        List<Trim> allTrims = productService.getAllTrims();
        List<Dealer> allDealers = dealerService.getAllDealers();

        model.addAttribute("inventory", inv);
        model.addAttribute("allTrims", allTrims);
        model.addAttribute("allDealers", allDealers);
        model.addAttribute("isEdit", true);
        model.addAttribute("active", "inventory");
        model.addAttribute("pageTitle", "Edit Inventory");

        return "admin/inventory/form";
    }

    // 4. Save (create hoặc update)
    @PostMapping
    public String saveInventory(
            @Valid @ModelAttribute("inventory") Inventory inventory,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        String username = (authentication != null) ? authentication.getName() : "system";

        // DEBUG LOG (tạm thời để biết nó bind ra gì)
        System.out.println(">>> SUBMIT INVENTORY:");
        System.out.println("    inventory.id       = " + inventory.getId());
        System.out.println("    dealer = " + (inventory.getDealer() == null ? "null" : inventory.getDealer().getId()));
        System.out.println("    trim   = " + (inventory.getTrim() == null   ? "null" : inventory.getTrim().getId()));
        System.out.println("    qtyOnHand          = " + inventory.getQtyOnHand());

        // load dropdown lại cho view (nếu có lỗi)
        List<Trim> allTrims = productService.getAllTrims();
        List<Dealer> allDealers = dealerService.getAllDealers();

        // validate người dùng có chọn dealer + trim
        if (inventory.getTrim() == null || inventory.getTrim().getId() == null) {
            bindingResult.rejectValue("trim", "trim.required", "Please select a Trim");
        }
        if (inventory.getDealer() == null || inventory.getDealer().getId() == null) {
            bindingResult.rejectValue("dealer", "dealer.required", "Please select a Dealer");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allTrims", allTrims);
            model.addAttribute("allDealers", allDealers);
            model.addAttribute("active", "inventory");
            model.addAttribute("isEdit", inventory.getId() != null);
            model.addAttribute("pageTitle",
                    (inventory.getId() != null) ? "Edit Inventory" : "Create Inventory");
            return "admin/inventory/form";
        }

        // CREATE
        if (inventory.getId() == null) {
            inventoryService.createInventory(inventory, username);
        }
        // UPDATE
        else {
            InventoryUpdateRequest req = new InventoryUpdateRequest();
            req.setId(inventory.getId());
            req.setQtyOnHand(inventory.getQtyOnHand());
            req.setNote("Manual update from Inventory form");
            inventoryService.updateInventory(req, username);
        }

        return "redirect:/admin/inventory?success=1";
    }

    @GetMapping("/{id}/history")
    public String viewHistory(@PathVariable Long id, Model model) {

        Inventory inv = inventoryService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + id));

        model.addAttribute("inventory", inv);
        model.addAttribute("history", inventoryService.getAdjustmentsForInventory(id));
        model.addAttribute("pageTitle", "Inventory History");
        model.addAttribute("active", "inventory");

        return "admin/inventory/history";
    }


    // 5. Delete
    @PostMapping("/{id}/delete")
    public String deleteInventory(@PathVariable Long id) {
        inventoryService.delete(id);
        return "redirect:/admin/inventory?success=1";
    }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.service.EvmInventoryService;
import com.uth.ev_dms.service.dto.AdjustInventoryForm;
import com.uth.ev_dms.service.dto.TransferToDealerForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/evm/inventory")
public class EvmInventoryController {

    private final EvmInventoryService service;

    public EvmInventoryController(EvmInventoryService service) {
        this.service = service;
    }

    // ===== List tồn kho HQ =====
    @GetMapping
    public String list(Model model) {
        List<Inventory> hqItems = service.listEvmInventory();
        model.addAttribute("items", hqItems);
        model.addAttribute("active", "inventory");
        model.addAttribute("pageTitle", "EVM Inventory");
        return "evm/inventory/list";
    }

    // ===== Điều chỉnh (±) =====
    @GetMapping("/{id}/adjust")
    public String adjustForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.loadAdjustForm(id));
        model.addAttribute("pageTitle", "Adjust Inventory");
        model.addAttribute("active", "inventory");
        return "evm/inventory/adjust";
    }

    @PostMapping("/{id}/adjust")
    public String doAdjust(@PathVariable Long id,
                           @ModelAttribute("form") AdjustInventoryForm form) {
        // id trong URL và id trong form phải trùng, nên dùng form là đủ
        service.adjustInventory(form);
        return "redirect:/evm/inventory";
    }

    // ===== Transfer về dealer =====
    @GetMapping("/{id}/transfer")
    public String showTransferForm(@PathVariable Long id, Model model) {
        try {
            TransferToDealerForm form = service.loadTransferForm(id);
            model.addAttribute("form", form);
            model.addAttribute("dealers", service.listDealers());
            model.addAttribute("active", "inventory");
            model.addAttribute("pageTitle", "Transfer to Dealer");
            return "evm/inventory/transfer";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/evm/inventory";
        }
    }

    @PostMapping("/{id}/transfer")
    public String doTransfer(@PathVariable Long id,
                             @ModelAttribute("form") TransferToDealerForm form,
                             Authentication auth,
                             Model model) {

        try {
            service.transferToDealer(id, form);
            return "redirect:/evm/inventory?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("dealers", service.listDealers());
            model.addAttribute("active", "inventory");
            model.addAttribute("pageTitle", "Transfer to Dealer");
            return "evm/inventory/transfer";
        }
    }

    // ===== Lịch sử điều chỉnh =====
    @GetMapping("/{id}/history")
    public String history(@PathVariable Long id, Model model) {
        model.addAttribute("inv", service.getInventory(id));
        model.addAttribute("events", service.listAdjustments(id));
        model.addAttribute("pageTitle", "Inventory History");
        model.addAttribute("active", "inventory");
        return "evm/inventory/history";
    }
}

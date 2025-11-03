package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.dto.AdjustInventoryForm;
import com.uth.ev_dms.service.dto.TransferToDealerForm;
import com.uth.ev_dms.service.EvmInventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/evm/inventory")
public class EvmInventoryController {

    private final EvmInventoryService service;

    public EvmInventoryController(EvmInventoryService service) {
        this.service = service;
    }

    // List tồn kho EVM
    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", service.listEvmInventories());
        model.addAttribute("pageTitle", "EVM Inventory");
        model.addAttribute("active", "inventory");
        return "evm/inventory/list";
    }

    // ----- Điều chỉnh (±) -----
    @GetMapping("/{id}/adjust")
    public String adjustForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.buildAdjustForm(id));
        model.addAttribute("pageTitle", "Adjust Inventory");
        model.addAttribute("active", "inventory");
        return "evm/inventory/adjust";
    }

    @PostMapping("/{id}/adjust")
    public String doAdjust(@PathVariable Long id,
                           @ModelAttribute("form") AdjustInventoryForm form) {
        service.adjustEvmInventory(id, form);
        return "redirect:/evm/inventory";
    }

    // ----- Transfer về dealer -----
    @GetMapping("/{id}/transfer")
    public String transferForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.buildTransferForm(id));
        model.addAttribute("dealers", service.listDealers()); // cho select2
        model.addAttribute("pageTitle", "Transfer to Dealer");
        model.addAttribute("active", "inventory");
        return "evm/inventory/transfer";
    }

    @PostMapping("/{id}/transfer")
    public String doTransfer(@PathVariable Long id,
                             @ModelAttribute("form") TransferToDealerForm form) {
        service.transferToDealer(id, form);
        return "redirect:/evm/inventory";
    }

    // ----- Lịch sử -----
    @GetMapping("/{id}/history")
    public String history(@PathVariable Long id, Model model) {
        model.addAttribute("inv", service.getInventory(id));
        model.addAttribute("events", service.listAdjustments(id));
        model.addAttribute("pageTitle", "Inventory History");
        model.addAttribute("active", "inventory");
        return "evm/inventory/history"; // có thể reuse template history cũ
    }
}

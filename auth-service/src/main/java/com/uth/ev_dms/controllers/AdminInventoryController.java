package com.uth.ev_dms.controllers;

import com.uth.ev_dms.client.InventoryClient;
import com.uth.ev_dms.client.dto.InventoryDto;
import com.uth.ev_dms.domain.InventoryAdjustment;
import com.uth.ev_dms.repo.DealerRepo;
import com.uth.ev_dms.repo.TrimRepo;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryClient inventoryClient;
    private final TrimRepo trimRepo;
    private final DealerRepo dealerRepo;

    // ================= LIST =================
    @GetMapping
    public String list(Model model) {
        // Lấy toàn bộ inventory từ inventory-service
        List<InventoryDto> all = inventoryClient.getAllInventories();

        // HQ: dealerId == null & branchId == null
        List<InventoryDto> hq = all.stream()
                .filter(inv -> inv.getDealerId() == null && inv.getBranchId() == null)
                .collect(Collectors.toList());

        // Branch: có dealerId (và thường có branchId)
        List<InventoryDto> branch = all.stream()
                .filter(inv -> inv.getDealerId() != null)
                .collect(Collectors.toList());

        model.addAttribute("hqInventories", hq);
        model.addAttribute("branchInventories", branch);

        return "admin/inventory/list";
    }

    // ================= CREATE =================
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("inventory", new InventoryDto());
        model.addAttribute("allTrims", trimRepo.findAll());
        model.addAttribute("allDealers", dealerRepo.findAll());// BẮT BUỘC
        return "admin/inventory/form";
    }


    @PostMapping
    public String createSubmit(@ModelAttribute InventoryDto dto) {
        inventoryClient.createInventory(dto);
        return "redirect:/admin/inventory";
    }

    // ================= EDIT =================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        InventoryDto inv = inventoryClient.getInventoryById(id);

        model.addAttribute("inventory", inv);
        // form dùng cho th:object trong edit.html
        model.addAttribute("form", inv);
        model.addAttribute("isEdit", true);

        return "admin/inventory/edit";
    }

    @PostMapping("/{id}/edit")
    public String editSubmit(
            @PathVariable("id") Long id,
            @ModelAttribute InventoryUpdateRequest req
    ) {
        inventoryClient.updateInventory(id, req);
        return "redirect:/admin/inventory";
    }

    // ================= HISTORY =================
    @GetMapping("/{id}/history")
    public String history(@PathVariable("id") Long id, Model model) {
        InventoryDto inv = inventoryClient.getInventoryById(id);
        List<InventoryAdjustment> history = inventoryClient.getHistory(id);

        model.addAttribute("inventory", inv);
        model.addAttribute("history", history);

        return "admin/inventory/history";
    }

    // ================= DELETE (nếu cần) =================
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        inventoryClient.deleteInventory(id);
        return "redirect:/admin/inventory";
    }
}

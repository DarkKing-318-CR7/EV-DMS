//package com.uth.ev_dms.controllers;
//
//import com.uth.ev_dms.domain.DealerBranch;
//import com.uth.ev_dms.domain.Inventory;
//import com.uth.ev_dms.repo.DealerBranchRepo;
//import com.uth.ev_dms.service.AuthService;
//import com.uth.ev_dms.service.InventoryService;
//import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/dealer/inventory")
//@RequiredArgsConstructor
//public class DealerInventoryController {
//    private final InventoryService inventoryService;
//    private final DealerBranchRepo dealerBranchRepo;
//    private final AuthService authService; // lấy dealerId từ user login
//
//    @GetMapping
//    public String list(Model model) {
//        Long dealerId = authService.getCurrentDealerId();
//        DealerBranch branch = dealerBranchRepo.findByDealerId(dealerId)
//                .orElseThrow(() -> new IllegalStateException("No branch found"));
//        List<Inventory> invs = inventoryService.findByBranchId(branch.getId());
//        model.addAttribute("inventories", invs);
//        return "dealer/inventory/list";
//    }
//
//    @GetMapping("/{id}/edit")
//    public String edit(@PathVariable Long id, Model model) {
//        Inventory inv = inventoryService.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));
//        model.addAttribute("inventory", inv);
//        return "dealer/inventory/edit";
//    }
//
//    @PostMapping("/update")
//    public String update(@ModelAttribute InventoryUpdateRequest req,
//                         @AuthenticationPrincipal UserDetails user) {
//        inventoryService.updateInventory(req, user.getUsername());
//        return "redirect:/dealer/inventory?success";
//    }
//}

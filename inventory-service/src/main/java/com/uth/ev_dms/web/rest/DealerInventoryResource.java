package com.uth.ev_dms.web.rest;

import com.uth.ev_dms.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class DealerInventoryResource {

    private final InventoryService inventoryService;

    /**
     * GET /api/inventory/dealer/{dealerId}/stock-by-trim
     * Trả về Map<trimId, qtyOnHand> cho 1 dealer (tổng tất cả branch).
     */
    @GetMapping("/dealer/{dealerId}/stock-by-trim")
//    @PreAuthorize("hasAnyAuthority(\"ROLE_ADMIN\", \"ROLE_DEALER\")")
    public ResponseEntity<Map<Long, Integer>> getStockByTrimForDealer(
            @PathVariable Long dealerId
    ) {
        return ResponseEntity.ok(inventoryService.getStockByTrimForDealer(dealerId));
    }

    /**
     * GET /api/inventory/branch/{branchId}/stock-by-trim
     * Trả về Map<trimId, qty> cho 1 chi nhánh.
     * Giá trị qty dựa trên sumAvailableByTrimAtBranch (tuỳ bạn đang tính available hay onHand).
     */
    @GetMapping("/branch/{branchId}/stock-by-trim")
//    @PreAuthorize("hasAnyAuthority(\"ROLE_ADMIN\", \"ROLE_DEALER\")")
    public ResponseEntity<Map<Long, Integer>> getStockByTrimForBranch(
            @PathVariable Long branchId
    ) {
        return ResponseEntity.ok(inventoryService.getStockByTrimForBranch(branchId));
    }
}

package com.uth.ev_dms.web.rest;

import com.uth.ev_dms.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/orders")
@RequiredArgsConstructor
public class InventoryRestController {

    private final InventoryService inventoryService;

    /**
     * POST /api/inventory/orders/{orderId}/allocate
     * Dùng khi tạo/confirm đơn: reserve toàn bộ items trong đơn hàng.
     */
    @PostMapping("/{orderId}/allocate")
    public ResponseEntity<Void> allocate(@PathVariable Long orderId) {
        inventoryService.allocateForOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/inventory/orders/{orderId}/ship
     * Khi giao hàng thành công → trừ onHand, giảm reserved.
     */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Void> ship(@PathVariable Long orderId) {
        inventoryService.shipForOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/inventory/orders/{orderId}/release
     * Khi huỷ đơn → trả lại reserved vào available.
     */
    @PostMapping("/{orderId}/release")
    public ResponseEntity<Void> release(@PathVariable Long orderId) {
        inventoryService.releaseForOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}

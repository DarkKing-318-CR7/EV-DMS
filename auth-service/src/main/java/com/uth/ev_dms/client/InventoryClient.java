package com.uth.ev_dms.client;

import com.uth.ev_dms.client.dto.InventoryDto;
import com.uth.ev_dms.domain.InventoryAdjustment;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "inventory-service-api",
        url = "${inventory-service.url}"
)
public interface InventoryClient {

    @GetMapping("/api/admin/inventories")
    List<InventoryDto> getAllInventories();

    @GetMapping("/api/admin/inventories/{id}")
    InventoryDto getInventoryById(@PathVariable("id") Long id);

    @PostMapping("/api/admin/inventories")
    InventoryDto createInventory(@RequestBody InventoryDto dto);

    @PutMapping("/api/admin/inventories/{id}")
    InventoryDto updateInventory(@PathVariable("id") Long id,
                                 @RequestBody InventoryUpdateRequest request);

    @DeleteMapping("/api/admin/inventories/{id}")
    void deleteInventory(@PathVariable("id") Long id);

    // Lịch sử điều chỉnh tồn kho
    @GetMapping("/api/admin/inventories/{id}/adjustments")
    List<InventoryAdjustment> getHistory(@PathVariable("id") Long id);
}
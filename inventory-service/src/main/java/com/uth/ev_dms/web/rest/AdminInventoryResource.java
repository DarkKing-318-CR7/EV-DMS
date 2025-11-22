package com.uth.ev_dms.web.rest;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.service.InventoryService;
import com.uth.ev_dms.service.dto.InventoryDto;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
import com.uth.ev_dms.service.mapper.InventoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/inventories")
@RequiredArgsConstructor
public class AdminInventoryResource {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    @GetMapping
    public ResponseEntity<List<InventoryDto>> getAllInventories() {
        List<InventoryDto> dtos = inventoryService.findAll()
                .stream()
                .map(inventoryMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDto> getInventory(@PathVariable("id") Long id) {
        return inventoryService.findById(id)
                .map(inventoryMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InventoryDto> createInventory(@RequestBody InventoryDto dto) {
        String username = "system"; // tạm

        Inventory entity = inventoryMapper.toEntity(dto);
        Inventory saved = inventoryService.createInventory(entity, username);

        return ResponseEntity
                .created(URI.create("/api/admin/inventories/" + saved.getId()))
                .body(inventoryMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable("id") Long id,
            @RequestBody InventoryUpdateRequest request
    ) {
        request.setId(id);
        String username = "system";

        Inventory updated = inventoryService.updateInventory(request, username);
        return ResponseEntity.ok(inventoryMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable("id") Long id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

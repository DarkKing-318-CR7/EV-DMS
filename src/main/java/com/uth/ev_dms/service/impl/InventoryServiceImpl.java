package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.InventoryAdjustment;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.repo.InventoryAdjustmentRepo;
import com.uth.ev_dms.repo.InventoryRepo;
import com.uth.ev_dms.service.InventoryService;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepo inventoryRepo;
    private final InventoryAdjustmentRepo inventoryAdjustmentRepo;

    // ===============================
    // ========== ORDER FLOW =========
    // ===============================
    @Override
    public boolean allocateForOrder(OrderItem item) {
        // Ý tưởng: tìm 1 record tồn kho tương ứng với trim trong OrderItem
        // và trừ số lượng.
        // Tuỳ DB bạn: item có getTrimId() hay getVehicleId()?
        // Trong hình bạn filter theo item.getTrimId() -> mình sẽ theo hướng đó.

        Long trimId = item.getTrimId(); // nếu code bạn hiện là getTrimId()
        Integer requestedQty = item.getQty();

        if (requestedQty == null || requestedQty <= 0) {
            return true; // không có gì để allocate
        }

        // Tìm inventory phù hợp (ví dụ kho EVM)
        Inventory inv = inventoryRepo.findAll()
                .stream()
                .filter(i ->
                        i.getTrim() != null &&
                                i.getTrim().getId().equals(trimId)
                )
                .findFirst()
                .orElse(null);

        if (inv == null) {
            System.out.println("[InventoryService] Không tìm thấy tồn kho cho trimId=" + trimId);
            return false;
        }

        Integer onHand = inv.getQtyOnHand();
        if (onHand == null) onHand = 0;

        if (onHand < requestedQty) {
            System.out.println("[InventoryService] Không đủ hàng. onHand=" + onHand +
                    ", cần=" + requestedQty + " (trimId=" + trimId + ")");
            return false;
        }

        // đủ -> trừ tồn
        inv.setQtyOnHand(onHand - requestedQty);
        inventoryRepo.save(inv);

        System.out.println("[InventoryService] Đã allocate " + requestedQty +
                " cho trimId=" + trimId + ". Còn lại=" + inv.getQtyOnHand());

        return true;
    }

    @Override
    public void releaseForOrder(Long orderId) {
        // TODO: nếu sau này muốn hoàn kho khi hủy đơn
        // Hiện tạm thời không implement logic, chỉ log.
        System.out.println("[InventoryService] releaseForOrder(orderId=" + orderId + ") chưa implement hoàn kho.");
    }


    // ===================================
    // ========== ADMIN INVENTORY ========
    // ===================================
    @Override
    public List<Inventory> findAll() {
        return inventoryRepo.findAll();
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return inventoryRepo.findById(id);
    }

    @Override
    public Inventory save(Inventory inv) {
        return inventoryRepo.save(inv);
    }

    @Override
    public void delete(Long id) {
        inventoryRepo.deleteById(id);
    }

    @Override
    @Transactional
    public Inventory createInventory(Inventory inv, String createdBy) {

        // onInsert() của entity sẽ lo createdAt/updatedAt/locationType/default qty
        Inventory saved = inventoryRepo.save(inv);

        // log nhập kho ban đầu
        Integer onHand = (saved.getQtyOnHand() == null) ? 0 : saved.getQtyOnHand();
        if (onHand > 0) {
            LocalDateTime now = LocalDateTime.now();

            InventoryAdjustment adj = new InventoryAdjustment();
            adj.setInventory(saved);
            adj.setDeltaQty(onHand);     // từ 0 lên onHand
            adj.setReason("Initial stock");
            adj.setCreatedAtEvent(now);
            adj.setCreatedAt(now);
            adj.setUpdatedAt(now);
            adj.setCreatedBy(createdBy);
            adj.setUpdatedBy(createdBy);

            inventoryAdjustmentRepo.save(adj);
        }

        return saved;
    }

    @Override
    @Transactional
    public Inventory updateInventory(InventoryUpdateRequest req, String updatedBy) {

        Inventory current = inventoryRepo.findById(req.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Inventory not found: " + req.getId())
                );

        Integer oldQty = (current.getQtyOnHand() == null) ? 0 : current.getQtyOnHand();
        Integer newQty = (req.getQtyOnHand() == null) ? 0 : req.getQtyOnHand();

        // cập nhật số lượng
        current.setQtyOnHand(req.getQtyOnHand());
        // giữ đồng bộ cột quantity
        current.setQuantity(req.getQtyOnHand());

        // updatedAt sẽ được set trong @PreUpdate
        Inventory saved = inventoryRepo.save(current);

        int delta = newQty - oldQty;
        if (delta != 0) {
            LocalDateTime now = LocalDateTime.now();

            InventoryAdjustment adj = new InventoryAdjustment();
            adj.setInventory(saved);
            adj.setDeltaQty(delta);
            adj.setReason(req.getNote());
            adj.setCreatedAtEvent(now);
            adj.setCreatedAt(now);
            adj.setUpdatedAt(now);
            adj.setCreatedBy(updatedBy);
            adj.setUpdatedBy(updatedBy);

            inventoryAdjustmentRepo.save(adj);
        }

        return saved;
    }

    @Override
    public List<InventoryAdjustment> getAdjustmentsForInventory(Long inventoryId) {
        return inventoryAdjustmentRepo.findByInventoryIdOrderByCreatedAtEventDesc(inventoryId);
    }


    @Override
    public Map<Long, Integer> getStockByTrimForDealer(Long dealerId) {
        var invList = inventoryRepo.findByDealer_Id(dealerId);

        Map<Long, Integer> stockMap = new HashMap<>();
        for (var inv : invList) {
            if (inv.getTrim() == null) continue;

            Long trimId = inv.getTrim().getId();
            Integer qty = inv.getQtyOnHand() == null ? 0 : inv.getQtyOnHand();

            stockMap.merge(trimId, qty, Integer::sum);
        }

        System.out.println("=== SERVICE stockMap ===");
        stockMap.forEach((tid, q) ->
                System.out.println("trimId=" + tid + " qty=" + q)
        );

        return stockMap;
    }


}

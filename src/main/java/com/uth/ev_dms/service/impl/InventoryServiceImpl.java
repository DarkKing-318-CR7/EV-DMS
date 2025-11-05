package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.repo.InventoryRepo;
import com.uth.ev_dms.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepo inventoryRepo;

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
    public List<Inventory> listAll() {
        return inventoryRepo.findAll();
    }

    @Override
    public Inventory getOrThrow(Long id) {
        return inventoryRepo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy Inventory id=" + id));
    }

    @Override
    public Inventory save(Inventory inv) {
        // Ở đây có thể validate nhẹ: không âm qty, locationType = "EVM" nếu null...
        if (inv.getQtyOnHand() == null || inv.getQtyOnHand() < 0) {
            inv.setQtyOnHand(0);
        }
        if (inv.getLocationType() == null || inv.getLocationType().isBlank()) {
            inv.setLocationType("EVM"); // default kho tổng
        }
        return inventoryRepo.save(inv);
    }

    @Override
    public void delete(Long id) {
        inventoryRepo.deleteById(id);
    }
}

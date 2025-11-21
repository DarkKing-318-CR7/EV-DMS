//package com.uth.ev_dms.service.impl;
//
//import com.uth.ev_dms.domain.Inventory;
//import com.uth.ev_dms.domain.InventoryAdjustment;
//import com.uth.ev_dms.domain.OrderItem;
//import com.uth.ev_dms.service.InventoryService;
//import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Service // KHÔNG @Primary — chỉ là bản giả lập
//@Deprecated // dùng cho test/mock
//public class InventoryServiceNoop implements InventoryService {
//
//    // ===== ORDER FLOW (mock) =====
//    @Override
//    public boolean allocateForOrder(OrderItem item) {
//        System.out.println("[NOOP] allocateForOrder(itemId=" + (item != null ? item.getId() : null) + ")");
//        return true; // luôn “thành công” để không chặn luồng
//    }
//
//    @Override
//    public void allocateForOrder(Long orderId) {
//        System.out.println("[NOOP] allocateForOrder(orderId=" + orderId + ")");
//    }
//
//    @Override
//    public void shipForOrder(Long orderId) {
//        System.out.println("[NOOP] shipForOrder(orderId=" + orderId + ")");
//    }
//
//    @Override
//    public void releaseForOrder(Long orderId) {
//        System.out.println("[NOOP] releaseForOrder(orderId=" + orderId + ")");
//    }
//
//    // ===== ADMIN INVENTORY (stub) =====
//    @Override
//    public List<Inventory> findAll() {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public Optional<Inventory> findById(Long id) {
//        return Optional.empty();
//    }
//
//    @Override
//    public Inventory save(Inventory inv) {
//        System.out.println("[NOOP] save(inv) — no-op");
//        return inv; // trả lại như cũ
//    }
//
//    @Override
//    public void delete(Long id) {
//        System.out.println("[NOOP] delete(id=" + id + ") — no-op");
//    }
//
//    @Override
//    public Inventory updateInventory(InventoryUpdateRequest req, String updatedBy) {
//        System.out.println("[NOOP] updateInventory(reqId=" + (req != null ? req.getId() : null) + ") — no-op");
//        return new Inventory(); // tránh NPE nếu ai đó gọi nhầm
//    }
//
//    @Override
//    public Inventory createInventory(Inventory inv, String createdBy) {
//        System.out.println("[NOOP] createInventory — no-op");
//        return inv;
//    }
//
//    @Override
//    public List<InventoryAdjustment> getAdjustmentsForInventory(Long inventoryId) {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public Map<Long, Integer> getStockByTrimForDealer(Long dealerId) {
//        return Collections.emptyMap();
//    }
//}

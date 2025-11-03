package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.InventoryAdjustment;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InventoryService {
    // được OrderServiceImpl dùng:
    boolean allocateForOrder(OrderItem item);
    void releaseForOrder(Long orderId);

    // thêm cho UI Admin Inventory:
    List<Inventory> findAll();

    Optional<Inventory> findById(Long id);

    Inventory save(Inventory inv);

    void delete(Long id);

    /**
     * Update tồn kho (số lượng, trạng thái...) và ghi log điều chỉnh.
     * @param req dữ liệu người dùng submit từ form
     * @param updatedBy username đang đăng nhập
     * @return inventory sau khi cập nhật
     */
    Inventory updateInventory(InventoryUpdateRequest req, String updatedBy);
    Inventory createInventory(Inventory inv, String createdBy);
    public List<InventoryAdjustment> getAdjustmentsForInventory(Long inventoryId);
    Map<Long, Integer> getStockByTrimForDealer(Long dealerId);
}

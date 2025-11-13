package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.InventoryAdjustment;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InventoryService {

    // ====== QUẢN LÝ TỒN KHO (Admin / EVM) ======

    List<Inventory> findAll();

    Optional<Inventory> findById(Long id);

    Inventory save(Inventory inv);

    void delete(Long id);

    /**
     * Cập nhật tồn kho và ghi log điều chỉnh
     * @param req dữ liệu người dùng nhập trên form
     * @param updatedBy username người thao tác
     */
    Inventory updateInventory(InventoryUpdateRequest req, String updatedBy);

    /**
     * Tạo mới inventory (ví dụ khi thêm dealer mới)
     */
    Inventory createInventory(Inventory inv, String createdBy);

    /**
     * Lấy danh sách log điều chỉnh của 1 inventory cụ thể
     */
    List<InventoryAdjustment> getAdjustmentsForInventory(Long inventoryId);

    /**
     * Lấy map số lượng tồn của từng Trim theo Dealer
     */
    Map<Long, Integer> getStockByTrimForDealer(Long dealerId);


    // ====== DÀNH CHO ORDER SERVICE ======

    /**
     * Legacy signature giữ lại cho tương thích cũ
     * Dùng để allocate từng item riêng lẻ
     */
    boolean allocateForOrder(OrderItem item);

    /**
     * Allocate nguyên đơn hàng (reserve tất cả hoặc fail toàn bộ)
     */
    void allocateForOrder(Long orderId);

    /**
     * Giao hàng → trừ tồn thực tế và giảm reserved
     */
    void shipForOrder(Long orderId);

    /**
     * Hủy đơn hàng → giải phóng tồn kho đã reserve
     */
    void releaseForOrder(Long orderId);
    public Map<Long,Integer> getStockByTrimForBranch(Long branchId);

}

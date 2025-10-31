package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.OrderItem;

public interface InventoryService {
    // được OrderServiceImpl dùng:
    boolean allocateForOrder(OrderItem item);
    void releaseForOrder(Long orderId);

    // thêm cho UI Admin Inventory:
    java.util.List<Inventory> listAll();
    Inventory getOrThrow(Long id);
    Inventory save(Inventory inv);
    void delete(Long id);
}

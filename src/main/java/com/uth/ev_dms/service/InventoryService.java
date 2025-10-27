package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.OrderItem;

public interface InventoryService {
    boolean allocateForOrder(OrderItem item);
    void releaseForOrder(Long orderId);
}

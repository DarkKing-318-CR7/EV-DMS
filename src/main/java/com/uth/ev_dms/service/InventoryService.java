package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.OrderItem;

public interface InventoryService {

    // Legacy signature kept for backward compatibility
    boolean allocateForOrder(OrderItem item);

    // Allocate atomically for the whole order (reserve all or fail)
    void allocateForOrder(Long orderId);

    // Ship allocated stock when delivering (reserved--, on_hand--)
    void shipForOrder(Long orderId);

    // Release reserved stock when deallocating / cancel
    void releaseForOrder(Long orderId);
}

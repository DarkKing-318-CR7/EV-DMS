package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.service.InventoryService;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceNoop implements InventoryService {

    @Override
    public boolean allocateForOrder(OrderItem item) {
        // Không làm gì (mock)
        return true;
    }

    @Override
    public void allocateForOrder(Long orderId) {
        // Không làm gì (mock)
    }

    @Override
    public void shipForOrder(Long orderId) {
        // Không làm gì (mock)
    }

    @Override
    public void releaseForOrder(Long orderId) {
        // Không làm gì (mock)
    }
}

package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.service.InventoryService;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceNoop implements InventoryService {

    @Override
    public boolean allocateForOrder(OrderItem item) {
        // Không thực hiện trừ tồn thật, chỉ log giả lập
        System.out.println("[NOOP] Allocate vehicleId=" + item.getVehicleId() + ", qty=" + item.getQty());
        return true; // luôn trả true để không chặn luồng allocate
    }

    @Override
    public void releaseForOrder(Long orderId) {
        // Không thực hiện trả tồn thật, chỉ log giả lập
        System.out.println("[NOOP] Release inventory for orderId=" + orderId);
    }
}

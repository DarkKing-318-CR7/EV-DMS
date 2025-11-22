package com.uth.ev_dms.service.dto;

import lombok.Data;

@Data
public class InventoryAdjustmentRequest {

    private Long inventoryId;   // id inventory
    private Integer deltaQty;   // +10 = nhập thêm, -5 = xuất bớt
    private String reason;      // lý do điều chỉnh
}

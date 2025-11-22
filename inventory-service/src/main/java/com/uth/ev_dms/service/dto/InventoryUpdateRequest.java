package com.uth.ev_dms.service.dto;

import lombok.Data;

@Data
public class InventoryUpdateRequest {

    private Long id;          // id của Inventory cần chỉnh
    private Integer qtyOnHand; // số lượng mới (trên form admin nhập)
    private String note;      // lý do điều chỉnh (hiển thị trong InventoryAdjustment)
}

package com.uth.ev_dms.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryUpdateRequest {

    @NotNull
    private Long id; // inventoryId

    @Min(value = 0, message = "Quantity on hand cannot be negative")
    private Integer quantityOnHand;

    @Min(value = 0, message = "Reserved quantity cannot be negative")
    private Integer qtyOnHand;

    // ví dụ: ACTIVE / HOLD / OUT_OF_STOCK
    private String status;

    // ghi chú lý do chỉnh sửa số lượng
    private String note;
}

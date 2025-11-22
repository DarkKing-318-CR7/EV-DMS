package com.uth.ev_dms.service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {

    private Long id;

    private Long dealerId;
    private Long branchId;

    private Long trimId;
    private String trimName;     // optional, cho FE hiển thị
    private String modelName;    // optional

    // DTO đặt tên dễ hiểu, khi map nhớ lấy từ entity.getQtyOnHand()
    private Integer qtyOnHand; // tổng trong kho
    private Integer reserved;       // đã giữ cho deal/đơn
    private Integer available;      // = quantityOnHand - reserved

    private String color;        // nếu bạn có field này trong entity
    private String vin;          // nếu bạn có field này

    private LocalDateTime updatedAt;
}

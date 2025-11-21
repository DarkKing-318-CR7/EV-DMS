package com.uth.ev_dms.service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Long id;
    private Long trimId;
    private String trimName;
    private String vehicleName;
    private BigDecimal unitPrice;
    private int qty;
    private BigDecimal discountAmount;
}

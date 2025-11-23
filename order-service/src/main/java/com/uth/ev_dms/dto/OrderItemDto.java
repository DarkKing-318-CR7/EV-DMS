package com.uth.ev_dms.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter@Getter
@Data
public class OrderItemDto {
    private Long id;
    private Long trimId;
    private String trimName;
    private String vehicleName;
    private BigDecimal unitPrice;
    private int qty;
    private BigDecimal discountAmount;
    private Long vehicleId;
    private BigDecimal totalPrice;

}

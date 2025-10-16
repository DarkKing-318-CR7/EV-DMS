package com.uth.ev_dms.sales.service.dto;

import java.math.BigDecimal;

public class CreateQuoteItemDTO {
    private Long vehicleId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}

package com.uth.ev_dms.client.dto;

import java.math.BigDecimal;

public class QuoteItemDto {

    private Long id;
    private Long trimId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTrimId() { return trimId; }
    public void setTrimId(Long trimId) { this.trimId = trimId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineAmount() { return lineAmount; }
    public void setLineAmount(BigDecimal lineAmount) { this.lineAmount = lineAmount; }
}

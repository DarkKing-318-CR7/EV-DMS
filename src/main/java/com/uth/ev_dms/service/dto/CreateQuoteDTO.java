package com.uth.ev_dms.service.dto;

import java.math.BigDecimal;
import java.util.List;

public class CreateQuoteDTO {
    private Long customerId;
    private Long dealerId;
    private Long vehicleTrimId;
    private String region;
    private String status;
    private BigDecimal totalAmount;
    private List<CreateQuoteItemDTO> items;

    // ===== Getters & Setters =====
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getDealerId() { return dealerId; }
    public void setDealerId(Long dealerId) { this.dealerId = dealerId; }

    public Long getVehicleTrimId() { return vehicleTrimId; }
    public void setVehicleTrimId(Long vehicleTrimId) { this.vehicleTrimId = vehicleTrimId; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public List<CreateQuoteItemDTO> getItems() { return items; }
    public void setItems(List<CreateQuoteItemDTO> items) { this.items = items; }
}

package com.uth.ev_dms.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class QuoteDto {

    private Long id;
    private Long dealerId;
    private Long dealerBranchId;
    private Long vehicleTrimId;
    private String region;

    private BigDecimal totalAmount;
    private BigDecimal appliedDiscount;
    private BigDecimal finalAmount;

    private String status;
    private String rejectComment;
    private LocalDateTime createdAt;

    private Long salesStaffId;
    private Long createdBy;

    private List<QuoteItemDto> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDealerId() { return dealerId; }
    public void setDealerId(Long dealerId) { this.dealerId = dealerId; }

    public Long getDealerBranchId() { return dealerBranchId; }
    public void setDealerBranchId(Long dealerBranchId) { this.dealerBranchId = dealerBranchId; }

    public Long getVehicleTrimId() { return vehicleTrimId; }
    public void setVehicleTrimId(Long vehicleTrimId) { this.vehicleTrimId = vehicleTrimId; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getAppliedDiscount() { return appliedDiscount; }
    public void setAppliedDiscount(BigDecimal appliedDiscount) { this.appliedDiscount = appliedDiscount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectComment() { return rejectComment; }
    public void setRejectComment(String rejectComment) { this.rejectComment = rejectComment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getSalesStaffId() { return salesStaffId; }
    public void setSalesStaffId(Long salesStaffId) { this.salesStaffId = salesStaffId; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public List<QuoteItemDto> getItems() { return items; }
    public void setItems(List<QuoteItemDto> items) { this.items = items; }
}

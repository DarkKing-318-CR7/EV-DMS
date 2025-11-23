package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "quote")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "dealer_id")
    private Long dealerId;

    @Column(name = "vehicle_trim_id")
    private Long vehicleTrimId;

    @Column(name = "region")
    private String region; // phục vụ validate promotion

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "applied_discount")
    private BigDecimal appliedDiscount; // tổng giảm

    @Column(name = "final_amount")
    private BigDecimal finalAmount; // totalAmount - appliedDiscount

    @Column(name = "status")
    private String status = "DRAFT";

    @Column(name = "reject_comment", length = 500)
    private String rejectComment; // lý do từ chối (manager)

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<QuoteItem> items = new ArrayList<>();


    @Column(name = "dealer_branch_id")
    private Long dealerBranchId;

    private Long salesStaffId;
    private Long createdBy;

    public Long getDealerBranchId() { return dealerBranchId; }
    public void setDealerBranchId(Long dealerBranchId) { this.dealerBranchId = dealerBranchId; }



    // ==========================
    // Getters & Setters
    // ==========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getDealerId() {
        return dealerId;
    }

    public void setDealerId(Long dealerId) {
        this.dealerId = dealerId;
    }

    public Long getVehicleTrimId() {
        return vehicleTrimId;
    }

    public void setVehicleTrimId(Long vehicleTrimId) {
        this.vehicleTrimId = vehicleTrimId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAppliedDiscount() {
        return appliedDiscount;
    }

    public void setAppliedDiscount(BigDecimal appliedDiscount) {
        this.appliedDiscount = appliedDiscount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectComment() {
        return rejectComment;
    }

    public void setRejectComment(String rejectComment) {
        this.rejectComment = rejectComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<QuoteItem> getItems() {
        return items;
    }

    public void setItems(List<QuoteItem> items) {
        this.items = items;
    }

    public Long getSalesStaffId() {
        return salesStaffId;
    }

    public void setSalesStaffId(Long salesStaffId) {
        this.salesStaffId = salesStaffId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}

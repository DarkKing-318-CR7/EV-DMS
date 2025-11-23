package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "order_hdr")
public class OrderHdr {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long dealerId;
    private Long dealerBranchId;   // 🔥 ADDED FIELD FIX ERROR
    private Long customerId;
    private Long quoteId;

    private Long salesStaffId;

    @Column(nullable = false, unique = true)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal depositAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private InstallmentPlan installmentPlan;

    @Column(name = "created_by")
    private Long createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // ===== Guard =====
    @PrePersist
    @PreUpdate
    private void ensureAmountsNotNull() {
        BigDecimal ZERO = BigDecimal.ZERO;

        if (totalAmount == null)   totalAmount = ZERO;
        if (depositAmount == null) depositAmount = ZERO;
        if (paidAmount == null)    paidAmount = ZERO;

        if (balanceAmount == null) {
            balanceAmount = totalAmount.subtract(depositAmount).subtract(paidAmount);
        }

        if (balanceAmount.compareTo(ZERO) < 0) {
            balanceAmount = ZERO;
        }

        if (status == null)    status = OrderStatus.NEW;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // ===== Helpers =====
    public void addItem(OrderItem it) { it.setOrder(this); items.add(it); }
    public void addPayment(Payment p) { p.setOrder(this); payments.add(p); }

    // ===== Getters/Setters =====
    public Long getId() { return id; }

    private String customerName;
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getDealerId() { return dealerId; }
    public void setDealerId(Long dealerId) { this.dealerId = dealerId; }

    public Long getDealerBranchId() { return dealerBranchId; }      // 🔥 ADDED
    public void setDealerBranchId(Long dealerBranchId) { this.dealerBranchId = dealerBranchId; } // 🔥 ADDED

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getQuoteId() { return quoteId; }
    public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public InstallmentPlan getInstallmentPlan() { return installmentPlan; }
    public void setInstallmentPlan(InstallmentPlan installmentPlan) { this.installmentPlan = installmentPlan; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Long getSalesStaffId() { return salesStaffId; }
    public void setSalesStaffId(Long salesStaffId) { this.salesStaffId = salesStaffId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public BigDecimal getBalanceAmount() { return balanceAmount; }
    public void setBalanceAmount(BigDecimal balanceAmount) { this.balanceAmount = balanceAmount; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(LocalDateTime allocatedAt) { this.allocatedAt = allocatedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}

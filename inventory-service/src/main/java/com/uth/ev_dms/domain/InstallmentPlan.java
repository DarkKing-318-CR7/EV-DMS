package com.uth.ev_dms.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "installment_plan")
public class InstallmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private com.uth.ev_dms.domain.OrderHdr order;

    private String bank;
    private Integer tenorMonths;        // số tháng trả góp
    private BigDecimal downPayment;     // trả trước
    private BigDecimal interestRate;    // lãi suất
    private BigDecimal monthlyAmount;   // số tiền mỗi kỳ
    private LocalDateTime approvedAt;
    private Integer months;
    private BigDecimal monthlyPayment;
    private BigDecimal totalPayable;




    public Long getId() {
        return id;
    }

    public com.uth.ev_dms.domain.OrderHdr getOrder() {
        return order;
    }

    public void setOrder(OrderHdr order) {
        this.order = order;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public Integer getTenorMonths() {
        return tenorMonths;
    }

    public void setTenorMonths(Integer tenorMonths) {
        this.tenorMonths = tenorMonths;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(BigDecimal monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Integer getMonths() { return months; }
    public void setMonths(Integer months) { this.months = months; }
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }
    public BigDecimal getTotalPayable() { return totalPayable; }
    public void setTotalPayable(BigDecimal totalPayable) { this.totalPayable = totalPayable; }

}

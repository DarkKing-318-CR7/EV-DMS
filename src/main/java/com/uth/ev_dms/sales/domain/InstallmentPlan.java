package com.uth.ev_dms.sales.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "installment_plan")
public class InstallmentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Integer months;
    private BigDecimal monthlyAmount;
    private BigDecimal interestRate;

    // Getters & Setters
}

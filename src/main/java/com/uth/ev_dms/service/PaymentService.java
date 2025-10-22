package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.InstallmentPlan;
import com.uth.ev_dms.domain.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    Payment addPayment(Long orderId, BigDecimal amount, String method, String note);
    BigDecimal computeMonthly(BigDecimal principal, int months, BigDecimal annualRatePct);
    InstallmentPlan createInstallment(Long orderId, int months, BigDecimal annualRate);
    List<Payment> findByOrderId(Long orderId);
}

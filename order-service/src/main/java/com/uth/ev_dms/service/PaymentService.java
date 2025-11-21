package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.domain.PaymentType;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    List<Payment> findByOrderId(Long orderId);

    Payment addPayment(Long orderId, BigDecimal amount, String method, String refNo, String note, PaymentType type);
    Payment addPayment(Long orderId, BigDecimal amount, String method, String refNo);

    Payment createInstallment(Long orderId, int tenorMonths, BigDecimal downPayment);
    // PaymentService.java
    boolean hasInstallment(Long orderId);
// ✅ 3 tham số
}

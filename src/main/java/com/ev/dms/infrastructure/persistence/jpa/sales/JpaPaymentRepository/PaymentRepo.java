package com.ev.dms.infrastructure.persistence.jpa.sales.JpaPaymentRepository;

import com.ev.dms.domain.sales.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
}

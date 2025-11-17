package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.domain.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentRepo extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder_IdOrderByPaidAtDesc(Long orderId);
    // PaymentRepo.java
    boolean existsByOrder_IdAndType(Long orderId, PaymentType type);

    @Query("SELECT SUM(p.amount) FROM Payment p")
    Long totalRevenue();


}


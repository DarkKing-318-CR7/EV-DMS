package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepo extends JpaRepository<Payment, Long> {

    @Query("select coalesce(sum(p.amount),0) from Payment p where p.order.id = :orderId")
    BigDecimal sumPaid(@Param("orderId") Long orderId);

    List<Payment> findByOrderId(Long orderId);
}

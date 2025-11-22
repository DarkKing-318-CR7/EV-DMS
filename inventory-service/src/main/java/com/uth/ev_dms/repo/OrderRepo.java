package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.OrderHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<OrderHdr, Long> {
    // Hiện tại inventory-service chỉ cần findById(orderId)
}

package com.ev.dms.infrastructure.persistence.jpa.sales.JpaOrderRepository;

import com.ev.dms.domain.sales.OrderHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<OrderHdr, Long> {
}

package com.uth.ev_dms.sales.repo;

import com.uth.ev_dms.sales.domain.OrderHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<OrderHdr, Long> {
}

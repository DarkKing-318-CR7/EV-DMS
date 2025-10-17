package com.ev.dms.domain.sales.repository;

import com.ev.dms.domain.sales.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {
}

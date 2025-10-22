// OrderItemRepo.java
package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}


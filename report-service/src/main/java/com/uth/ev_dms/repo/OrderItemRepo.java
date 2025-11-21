package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    // ⭐ Xe hot 7 ngày gần nhất (theo modelName)
    @Query("""
    SELECT t.trimName, COUNT(i.id)
    FROM OrderItem i
    JOIN Trim t ON t.id = i.trimId
    JOIN OrderHdr o ON o.id = i.order.id
    WHERE o.createdAt >= :weekAgo
    GROUP BY t.trimName
    ORDER BY COUNT(i.id) DESC
""")
    List<Object[]> findHotModelsThisWeek(@Param("weekAgo") LocalDateTime weekAgo);

}

package com.uth.ev_dms.report;

import com.uth.ev_dms.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COALESCE(SUM(o.totalPrice),0) FROM Order o")
    Double getTotalRevenue();

    @Query("SELECT COUNT(o.id) FROM Order o")
    Long getTotalOrders();

    // JPQL doesn't support LIMIT; use native query
    @Query(value = "SELECT v.model_name FROM orders o JOIN vehicles v ON o.vehicle_id = v.id GROUP BY v.model_name ORDER BY COUNT(o.id) DESC LIMIT 1", nativeQuery = true)
    String getTopVehicle();
}

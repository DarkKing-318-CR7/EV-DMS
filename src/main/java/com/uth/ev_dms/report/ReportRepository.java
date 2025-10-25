package com.uth.ev_dms.report;

import com.uth.ev_dms.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Order, Long> {

    // Tổng doanh thu
    @Query("SELECT SUM(o.totalPrice) FROM Order o")
    Double getTotalRevenue();

    // Tổng số đơn hàng
    @Query("SELECT COUNT(o.id) FROM Order o")
    Long getTotalOrders();

    // Xe bán chạy nhất
    @Query("SELECT o.vehicle.modelName FROM Order o GROUP BY o.vehicle.modelName ORDER BY COUNT(o.id) DESC LIMIT 1")
    String getTopVehicle();
}

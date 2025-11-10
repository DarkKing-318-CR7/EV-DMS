package com.uth.ev_dms.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ReportService {

    private final JdbcTemplate jdbc;

    public ReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Sales summary by day (last 30 days)
    public List<Map<String, Object>> getSalesByDay(int days) {
        String sql = "SELECT DATE(o.created_at) as day, " +
                "COALESCE(SUM(o.total_amount),0) as total_amount, " +
                "COUNT(o.id) as orders_count " +
                "FROM order_hdr o " +
                "WHERE o.created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                "GROUP BY DATE(o.created_at) ORDER BY DATE(o.created_at)";
        return jdbc.queryForList(sql, days);
    }

    // Vehicles inventory summary
    public List<Map<String, Object>> getVehiclesInventory() {
        String sql = "SELECT v.id, v.model_name, t.trim_name, COALESCE(i.quantity,0) as qty " +
                "FROM vehicles v " +
                "LEFT JOIN trims t ON t.vehicle_id = v.id " +
                "LEFT JOIN inventories i ON i.trim_id = t.id " +
                "ORDER BY v.model_name";
        return jdbc.queryForList(sql);
    }

    // Custom report example: top-selling trims
    public List<Map<String, Object>> getTopSellingTrims(int limit) {
        String sql = "SELECT ti.vehicle_id, t.trim_name, SUM(oi.qty) as sold_qty " +
                "FROM order_item oi " +
                "JOIN trims t ON oi.trim_id = t.id " +
                "JOIN order_hdr o ON oi.order_id = o.id " +
                "GROUP BY oi.trim_id ORDER BY sold_qty DESC LIMIT ?";
        return jdbc.queryForList(sql, limit);
    }
}

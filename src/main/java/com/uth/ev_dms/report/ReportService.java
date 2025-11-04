package com.uth.ev_dms.report;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private final ReportRepository repo;

    public ReportService(ReportRepository repo) {
        this.repo = repo;
    }

    public Map<String, Object> getSalesReport() {
        Map<String, Object> m = new HashMap<>();
        Double revenue = repo.getTotalRevenue();
        Long orders = repo.getTotalOrders();
        String topVehicle = repo.getTopVehicle();
        m.put("totalRevenue", revenue == null ? 0 : revenue);
        m.put("totalOrders", orders == null ? 0 : orders);
        m.put("topVehicle", topVehicle == null ? "N/A" : topVehicle);
        return m;
    }
}

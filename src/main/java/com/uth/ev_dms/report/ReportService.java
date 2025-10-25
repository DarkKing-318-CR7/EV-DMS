package com.uth.ev_dms.report;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class ReportService {
    public Map<String, Object> getSalesReport() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRevenue", 12000000);
        data.put("totalOrders", 250);
        data.put("topVehicle", "EV Model X");
        return data;
    }
}

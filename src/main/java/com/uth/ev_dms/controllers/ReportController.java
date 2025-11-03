package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class ReportController {

    // === TRANG CHÍNH: Dashboard Reports ===
    @GetMapping("/reports")
    public String reportDashboard() {
        return "reports/report"; // tương ứng report.html
    }

    // === TRANG BÁO CÁO BÁN HÀNG ===
    @GetMapping("/reports/sale")
    public String salesReport(
            @RequestParam(required = false) String dealer,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model) {

        // Demo dữ liệu giả
        List<Map<String, Object>> sales = new ArrayList<>();

        sales.add(createSale("Dealer A", "Model X", 5, 50000.0, "2025-11-01"));
        sales.add(createSale("Dealer B", "Model Y", 3, 36000.0, "2025-11-02"));
        sales.add(createSale("Dealer C", "Model Z", 7, 84000.0, "2025-10-30"));

        // Gửi dữ liệu sang view
        model.addAttribute("sales", sales);
        return "reports/sale"; // tương ứng sale.html
    }

    private Map<String, Object> createSale(String dealer, String model, int qty, double total, String date) {
        Map<String, Object> map = new HashMap<>();
        map.put("dealerName", dealer);
        map.put("vehicleModel", model);
        map.put("quantity", qty);
        map.put("total", total);
        map.put("date", date);
        return map;
    }

    // === TRANG BÁO CÁO PHƯƠNG TIỆN ===
    @GetMapping("/reports/vehicles")
    public String vehicleReport(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String status,
            Model m) {

        List<Map<String, Object>> vehicles = new ArrayList<>();
        vehicles.add(createVehicle("VIN12345", "Model X", "Premium", "Available", "Dealer A"));
        vehicles.add(createVehicle("VIN67890", "Model Y", "Standard", "Sold", "Dealer B"));
        vehicles.add(createVehicle("VIN11111", "Model Z", "Luxury", "Allocated", "Dealer C"));

        m.addAttribute("vehicles", vehicles);
        return "reports/vehicles"; // tương ứng vehicles.html
    }

    private Map<String, Object> createVehicle(String vin, String model, String trim, String status, String dealer) {
        Map<String, Object> v = new HashMap<>();
        v.put("vin", vin);
        v.put("model", model);
        v.put("trim", trim);
        v.put("status", status);
        v.put("dealerName", dealer);
        return v;
    }
}

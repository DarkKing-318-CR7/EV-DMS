package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class ReportWebController {

    private final OrderRepo orderRepo;
    private final VehicleRepo vehicleRepo;

    @GetMapping({"/reports", "/dealer/reports"})
    public String reportDashboard(Model model) {
        model.addAttribute("active", "reports");
        model.addAttribute("pageTitle", "Reports Dashboard");
        return "reports/report";
    }

    @GetMapping({"/reports/sale", "/reports/advanced-sale"})
    public String salesReport(
            @RequestParam(name = "dealer", required = false) String dealer,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            Model model
    ) {
        List<OrderHdr> orders = orderRepo.findAll();
        long totalOrders = orders.size();
        BigDecimal totalRevenue = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> sales = new ArrayList<>();
        for (OrderHdr o : orders) {
            Map<String, Object> row = new HashMap<>();
            row.put("dealerName", o.getDealerId() != null ? "Đại lý #" + o.getDealerId() : "Trực tiếp");
            row.put("orderId", o.getId());
            row.put("customerName", o.getCustomerName() != null ? o.getCustomerName() : "Khách hàng #" + o.getCustomerId());
            row.put("quantity", o.getItems() != null ? o.getItems().size() : 1);
            row.put("totalRevenue", o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
            row.put("status", o.getStatus() != null ? o.getStatus().name() : "N/A");
            row.put("orderDate", o.getCreatedAt() != null ? o.getCreatedAt().toLocalDate().toString() : "N/A");
            row.put("modelName", "VinFast VF 8 / VF 9");
            sales.add(row);
        }

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("topVehicle", "VinFast VF8 / EV6");
        model.addAttribute("sales", sales);
        model.addAttribute("dealer", dealer);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("active", "reports");
        model.addAttribute("pageTitle", "Sales Report");

        return "reports/advanced-sale";
    }

    @GetMapping("/reports/vehicles")
    public String vehicleReport(
            @RequestParam(name = "modelName", required = false) String modelName,
            @RequestParam(name = "status", required = false) String status,
            Model model
    ) {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        var vehicleList = vehicleRepo.findAll();
        int idx = 1;
        for (var v : vehicleList) {
            Map<String, Object> map = new HashMap<>();
            map.put("vin", "EV-VIN-" + (10000 + idx));
            map.put("model", v.getModelName());
            map.put("trim", v.getModelCode());
            map.put("status", idx % 2 == 0 ? "Available" : "Allocated");
            map.put("dealerName", "Đại lý EV #" + (idx % 3 + 1));
            vehicles.add(map);
            idx++;
        }

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("active", "reports");
        model.addAttribute("pageTitle", "Vehicle Inventory Report");
        return "reports/vehicles";
    }
}

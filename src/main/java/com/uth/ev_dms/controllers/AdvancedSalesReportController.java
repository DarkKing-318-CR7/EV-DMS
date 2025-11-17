package com.uth.ev_dms.controllers;

import com.uth.ev_dms.report.SalesReportRow;
import com.uth.ev_dms.report.SalesReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class AdvancedSalesReportController {

    private final SalesReportService salesReportService;

    public AdvancedSalesReportController(SalesReportService salesReportService) {
        this.salesReportService = salesReportService;
    }

    @GetMapping("/advanced-sale")
    public String advancedSaleReport(@RequestParam(required = false) String dealer,
                                     @RequestParam(required = false) String model,
                                     @RequestParam(required = false) String fromDate,
                                     @RequestParam(required = false) String toDate,
                                     Model mv) {

        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);

        // lấy dữ liệu từ service (hiện tại đang demo trong SalesReportServiceImpl)
        List<SalesReportRow> sales =
                salesReportService.getSalesReport(dealer, model, from, to);

        long totalOrders = 0L;
        double totalRevenue = 0.0;
        Map<String, Long> quantityByModel = new HashMap<>();

        for (SalesReportRow row : sales) {
            // tổng số lượng bán
            totalOrders += row.getQuantity();

            // tổng doanh thu
            if (row.getTotalRevenue() != null) {
                totalRevenue += row.getTotalRevenue().doubleValue();
            }

            // gom số lượng theo từng model
            String m = row.getModelName();
            if (m != null) {
                Long current = quantityByModel.getOrDefault(m, 0L);
                quantityByModel.put(m, current + row.getQuantity());
            }
        }

        // tìm model bán chạy nhất
        String topVehicle = "";
        long maxQty = 0L;
        for (Map.Entry<String, Long> entry : quantityByModel.entrySet()) {
            if (entry.getValue() > maxQty) {
                maxQty = entry.getValue();
                topVehicle = entry.getKey();
            }
        }

        // thông tin cho layout
        mv.addAttribute("pageTitle", "Sales Report (Advanced)");
        mv.addAttribute("active", "reports");

        // summary
        mv.addAttribute("totalOrders", totalOrders);
        mv.addAttribute("totalRevenue", totalRevenue);
        mv.addAttribute("topVehicle", topVehicle);

        // filter values
        mv.addAttribute("dealer", dealer);
        mv.addAttribute("model", model);
        mv.addAttribute("fromDate", fromDate);
        mv.addAttribute("toDate", toDate);

        // list detail
        mv.addAttribute("sales", sales);

        // dùng luôn view reports/sale.html
        return "reports/advanced-sale";
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
    }
}

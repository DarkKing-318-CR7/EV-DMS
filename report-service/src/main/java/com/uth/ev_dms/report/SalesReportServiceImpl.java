package com.uth.ev_dms.report;

import com.uth.ev_dms.client.OrderClient;
import com.uth.ev_dms.client.dto.OrderSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesReportServiceImpl implements SalesReportService {

    private final OrderClient orderClient;

    @Override
    public List<SalesReportRow> getSalesReport(String dealer,
                                               String model,
                                               LocalDate fromDate,
                                               LocalDate toDate) {

        // 1. Gọi order-service lấy danh sách orders trong khoảng thời gian
        List<OrderSummaryDto> orders =
                orderClient.listOrders(null, null, fromDate, toDate);

        List<SalesReportRow> result = new ArrayList<>();

        for (OrderSummaryDto o : orders) {

            // 2. Filter theo dealer (nếu user có chọn)
            if (dealer != null && !dealer.isBlank()) {
                String d = "Dealer " + o.getDealerId();
                if (!d.toLowerCase().contains(dealer.toLowerCase())) {
                    continue;
                }
            }

            // 3. Tạm thời chưa có modelName từ order-service,
            //    mình cho "N/A" hoặc sau này sẽ nối với inventory-service.
            String modelName = "N/A";
            if (model != null && !model.isBlank()) {
                // chưa có model thực, filter model tạm bỏ qua
                // continue; // nếu muốn hard filter
            }

            SalesReportRow row = new SalesReportRow();
            row.setDealerName("Dealer " + o.getDealerId());
            row.setModelName(modelName);
            row.setQuantity(1); // mỗi order = 1, sau này có thể sum số lượng item
            row.setTotalRevenue(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
            row.setDate(o.getCreatedAt() != null ? o.getCreatedAt().toLocalDate() : null);

            result.add(row);
        }

        return result;
    }
}

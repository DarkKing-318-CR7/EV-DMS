package com.uth.ev_dms.report;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesReportServiceImpl implements SalesReportService {

    @Override
    public List<SalesReportRow> getSalesReport(String dealer,
                                               String model,
                                               LocalDate fromDate,
                                               LocalDate toDate) {

        // TODO: sau này thay bằng query DB thật
        List<SalesReportRow> list = new ArrayList<>();

        SalesReportRow r1 = new SalesReportRow();
        r1.setDealerName("Dealer HCM");
        r1.setModelName("EV-100");
        r1.setQuantity(5);
        r1.setTotalRevenue(new BigDecimal("250000.00"));
        r1.setDate(LocalDate.now().minusDays(2));
        list.add(r1);

        SalesReportRow r2 = new SalesReportRow();
        r2.setDealerName("Dealer HN");
        r2.setModelName("EV-200");
        r2.setQuantity(3);
        r2.setTotalRevenue(new BigDecimal("210000.00"));
        r2.setDate(LocalDate.now().minusDays(1));
        list.add(r2);

        SalesReportRow r3 = new SalesReportRow();
        r3.setDealerName("Dealer HCM");
        r3.setModelName("EV-100");
        r3.setQuantity(2);
        r3.setTotalRevenue(new BigDecimal("100000.00"));
        r3.setDate(LocalDate.now());
        list.add(r3);

        // Ở bản demo này mình bỏ qua filter, chỉ return tất cả.
        // Khi bạn kết nối DB thật thì lọc ngay trong query.

        return list;
    }
}

package com.uth.ev_dms.report;

import java.time.LocalDate;
import java.util.List;

public interface SalesReportService {

    List<SalesReportRow> getSalesReport(String dealer,
                                        String model,
                                        LocalDate fromDate,
                                        LocalDate toDate);
}

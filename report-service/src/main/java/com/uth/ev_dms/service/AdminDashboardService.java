package com.uth.ev_dms.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AdminDashboardService {

    // TODO: sau này inject các Repo hoặc FeignClient vào đây để lấy số liệu thật

    public long totalDealers() {
        return 0L;
    }

    public long totalVehicles() {
        return 0L;
    }

    public long totalTrims() {
        return 0L;
    }

    public long totalPromotions() {
        return 0L;
    }

    public long totalOrders() {
        return 0L;
    }

    public long totalInventory() {
        return 0L;
    }

    public long totalUsers() {
        return 0L;
    }

    public long totalCustomers() {
        return 0L;
    }

    public long totalQuotes() {
        return 0L;
    }

    public BigDecimal totalRevenue() {
        return BigDecimal.ZERO;
    }
}

package com.uth.ev_dms.service;

public interface AdminDashboardService {

    long totalDealers();
    long totalVehicles();
    long totalTrims();
    long totalPromotions();
    long totalOrders();
    long totalInventory();
    long totalUsers();
    long totalCustomers();
    long totalQuotes();
    long totalRevenue(); // tổng doanh thu (từ payments)
}

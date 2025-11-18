package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.uth.ev_dms.repo.InventoryRepo;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final DealerRepo dealerRepo;
    private final VehicleRepo vehicleRepo;
    private final TrimRepo trimRepo;
    private final PromotionRepo promotionRepo;
    private final OrderRepo orderRepo;
    private final InventoryRepo inventoryRepo;
    private final UserRepository userRepo;
    private final CustomerRepo customerRepo;
    private final QuoteRepo quoteRepo;
    private final PaymentRepo paymentRepo;

    @Override public long totalDealers() { return dealerRepo.count(); }
    @Override public long totalVehicles() { return vehicleRepo.count(); }
    @Override public long totalTrims() { return trimRepo.count(); }
    @Override public long totalPromotions() { return promotionRepo.count(); }
    @Override public long totalOrders() { return orderRepo.count(); }
    @Override
    public long totalInventory() {
        Long qty = inventoryRepo.sumTotalQty();
        return qty == null ? 0 : qty;
    }

    @Override public long totalUsers() { return userRepo.count(); }
    @Override public long totalCustomers() { return customerRepo.count(); }
    @Override public long totalQuotes() { return quoteRepo.count(); }

    @Override
    public long totalRevenue() {
        Long v = paymentRepo.totalRevenue();
        return v == null ? 0 : v;
    }
}

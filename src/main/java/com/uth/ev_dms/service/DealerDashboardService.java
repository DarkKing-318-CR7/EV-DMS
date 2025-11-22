package com.uth.ev_dms.service;

import com.uth.ev_dms.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DealerDashboardService {

    private final InventoryRepo inventoryRepo;
    private final QuoteRepo quoteRepo;
    private final CustomerRepo customerRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;

    // LẤY dealerId từ user login
    public Long getDealerIdByUsername(String username) {
        return userRepo.findByUsername(username)
                .map(u -> u.getDealer().getId())
                .orElse(null);
    }

    // TỔNG HỢP KPI
    public Map<String, Object> getDealerStats(Long dealerId) {
        Map<String, Object> m = new HashMap<>();

        m.put("totalInventory", inventoryRepo.totalByDealer(dealerId));
        m.put("totalSold", orderRepo.countSoldByDealer(dealerId));
        m.put("totalCustomers", customerRepo.countByDealer(dealerId));
        m.put("totalQuotes", quoteRepo.countByDealerId(dealerId));
        m.put("quotePending", quoteRepo.countByDealerIdAndStatus(dealerId, "PENDING"));
        m.put("quoteApproved", quoteRepo.countByDealerIdAndStatus(dealerId, "APPROVED"));

        return m;
    }

    public List<Object[]> getInventoryByModel(Long dealerId) {
        return inventoryRepo.totalGroupByModel(dealerId);
    }

    public List<Object[]> getLowStockModels(Long dealerId) {
        return inventoryRepo.findLowStockModels(dealerId);
    }
}

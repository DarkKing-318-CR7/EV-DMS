package com.uth.ev_dms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.uth.ev_dms.repo.QuoteRepo;

@Service
@RequiredArgsConstructor
public class EvmDashboardService {

    private final QuoteRepo quoteRepo;

    public Long getRevenue() {
        Long total = quoteRepo.totalRevenue();
        return total != null ? total : 0L;
    }
}

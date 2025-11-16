package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepo extends JpaRepository<Quote, Long> {
    List<Quote> findByStatus(String status);
    List<Quote> findByDealerBranchId(Long branchId);
    List<Quote> findByDealerBranchIdAndStatus(Long branchId, String status);

}

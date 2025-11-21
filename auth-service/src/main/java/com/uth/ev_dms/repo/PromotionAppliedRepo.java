package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.PromotionApplied;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionAppliedRepo extends JpaRepository<PromotionApplied, Long> {
    List<PromotionApplied> findByQuoteId(Long quoteId);
}

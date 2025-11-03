package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.QuotePromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotePromotionRepo extends JpaRepository<QuotePromotion, Long> {
    List<QuotePromotion> findByQuoteId(Long quoteId);
}

package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteItemRepo extends JpaRepository<QuoteItem, Long> {
}

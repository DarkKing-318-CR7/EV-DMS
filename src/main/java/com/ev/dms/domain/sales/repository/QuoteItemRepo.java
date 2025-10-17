package com.ev.dms.domain.sales.repository;

import com.ev.dms.domain.sales.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteItemRepo extends JpaRepository<QuoteItem, Long> {
}

package com.ev.dms.infrastructure.persistence.jpa.sales.JpaQuoteRepository;

import com.ev.dms.domain.sales.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepo extends JpaRepository<Quote, Long> {
}

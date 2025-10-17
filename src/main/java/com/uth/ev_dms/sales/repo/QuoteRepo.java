package com.uth.ev_dms.sales.repo;

import com.uth.ev_dms.sales.domain.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepo extends JpaRepository<Quote, Long> {
}

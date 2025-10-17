package com.uth.ev_dms.reporting.repo;

import com.uth.ev_dms.reporting.demain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepo extends JpaRepository<Promotion, Long> {
}

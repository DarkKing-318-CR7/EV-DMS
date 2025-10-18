package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.InstallmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallmentPlanRepo extends JpaRepository<InstallmentPlan, Long> {
}

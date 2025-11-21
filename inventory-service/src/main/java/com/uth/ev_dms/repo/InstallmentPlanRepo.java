package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.InstallmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentPlanRepo extends JpaRepository<InstallmentPlan, Long> {
}

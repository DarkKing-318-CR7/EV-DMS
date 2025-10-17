package com.ev.dms.infrastructure.persistence.jpa.sales.JpaInstallmentPlanRepository;

import com.ev.dms.domain.sales.InstallmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallmentPlanRepo extends JpaRepository<InstallmentPlan, Long> {
}

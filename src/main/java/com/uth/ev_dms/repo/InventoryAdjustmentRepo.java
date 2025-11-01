package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryAdjustmentRepo extends JpaRepository<InventoryAdjustment, Long> {
}

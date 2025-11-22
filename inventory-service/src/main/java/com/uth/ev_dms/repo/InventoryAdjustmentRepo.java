package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryAdjustmentRepo extends JpaRepository<InventoryAdjustment, Long> {

    List<InventoryAdjustment> findByInventoryIdOrderByCreatedAtEventDesc(Long inventoryId);
}

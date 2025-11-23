package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.InventoryMove;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMoveRepo extends JpaRepository<InventoryMove, Long> {
}

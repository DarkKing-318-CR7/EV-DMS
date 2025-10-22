package com.uth.ev_dms.repo;



import com.uth.ev_dms.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepo extends JpaRepository<Inventory, Long> {
    List<Inventory> findByDealerId(Long dealerId);
    Optional<Inventory> findByDealerIdAndTrimId(Long dealerId, Long trimId);
}

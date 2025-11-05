package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Long> {
    // sau này có thể thêm findByTrimId(...) hoặc findByLocationType(...) nếu cần
}

package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Long> {
    List<Inventory> findByDealer_Id(Long dealerId);

    List<Inventory> findByLocationTypeOrderByUpdatedAtDesc(String locationType);

    Optional<Inventory> findByTrimIdAndLocationTypeAndDealerId(Long trimId, String locationType, Long dealerId);
}


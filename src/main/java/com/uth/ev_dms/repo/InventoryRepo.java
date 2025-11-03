package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Inventory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface InventoryRepo extends JpaRepository<Inventory, Long> {

    // khop entity hien co: dealer/trim la quan he -> dung .dealer.id / .trim.id
    @Query("select i from Inventory i where i.dealer.id = :dealerId")
    List<Inventory> findByDealerId(@Param("dealerId") Long dealerId);

    @Query("select i from Inventory i where i.dealer.id = :dealerId and i.trim.id = :trimId")
    Optional<Inventory> findByDealerIdAndTrimId(@Param("dealerId") Long dealerId,
                                                @Param("trimId") Long trimId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.dealer.id = :dealerId and i.trim.id = :trimId")
    Optional<Inventory> lockByDealerAndTrim(@Param("dealerId") Long dealerId,
                                            @Param("trimId") Long trimId);
}

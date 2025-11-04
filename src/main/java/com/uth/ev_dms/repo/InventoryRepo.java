package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Long> {

    /* ===== Truy vấn theo Dealer ===== */
    List<Inventory> findByDealer_Id(Long dealerId);

    @Query("select i from Inventory i where i.dealer.id = :dealerId and i.trim.id = :trimId")
    Optional<Inventory> findByDealerIdAndTrimId(@Param("dealerId") Long dealerId,
                                                @Param("trimId") Long trimId);

    /* Khoá ghi khi allocate để tránh race condition */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.dealer.id = :dealerId and i.trim.id = :trimId")
    Optional<Inventory> lockByDealerAndTrim(@Param("dealerId") Long dealerId,
                                            @Param("trimId") Long trimId);

    /* ===== Các truy vấn phục vụ UI/report ===== */

    // yêu cầu Inventory (hoặc BaseAudit) có mapped field updatedAt
    List<Inventory> findByLocationTypeOrderByUpdatedAtDesc(String locationType);

    // đúng path property: trim.id & dealer.id
    Optional<Inventory> findByTrim_IdAndLocationTypeAndDealer_Id(Long trimId,
                                                                 String locationType,
                                                                 Long dealerId);
    // --- Backward-compat alias for older callers ---
    default java.util.Optional<Inventory> findByTrimIdAndLocationTypeAndDealerId(
            Long trimId, String locationType, Long dealerId) {
        return findByTrim_IdAndLocationTypeAndDealer_Id(trimId, locationType, dealerId);
    }

}

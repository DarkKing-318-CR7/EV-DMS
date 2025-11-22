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

    // Lock 1 dòng tồn kho theo branch + trim để update an toàn (allocate/ship/release)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.branch.id = :branchId and i.trim.id = :trimId")
    Optional<Inventory> lockByBranchAndTrim(@Param("branchId") Long branchId,
                                            @Param("trimId") Long trimId);

    // Tính tổng available (onHand - reserved) theo trim tại 1 chi nhánh
    @Query("""
           select i.trim.id, sum(i.qtyOnHand - coalesce(i.reserved,0))
           from Inventory i
           where i.branch.id = :branchId
           group by i.trim.id
           """)
    List<Object[]> sumAvailableByTrimAtBranch(@Param("branchId") Long branchId);

    // Lấy tất cả inventory của 1 dealer (gộp nhiều chi nhánh)
    List<Inventory> findByDealer_Id(Long dealerId);

    // Lấy 1 dòng inventory duy nhất theo trim + branch
    Optional<Inventory> findByTrim_IdAndBranch_Id(Long trimId, Long branchId);
}

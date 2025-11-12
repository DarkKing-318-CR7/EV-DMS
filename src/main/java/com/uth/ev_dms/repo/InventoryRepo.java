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

    // Theo Dealer (giữ nguyên)
    List<Inventory> findByDealer_Id(Long dealerId);
    List<Inventory> findByDealer_IdOrderByIdDesc(Long dealerId);

    // ✅ Theo Branch
    List<Inventory> findByBranch_Id(Long branchId);

    // ✅ Tìm 1 dòng kho theo Trim + Branch (phục vụ upsert)
    Optional<Inventory> findByTrim_IdAndBranch_Id(Long trimId, Long branchId);

    // ✅ Liệt kê chung có filter tuỳ chọn
    @Query("""
        select i from Inventory i
        where (:dealerId is null or i.dealer.id = :dealerId)
          and (:branchId is null or i.branch.id = :branchId)
        order by i.id desc
    """)
    List<Inventory> findList(@Param("dealerId") Long dealerId,
                             @Param("branchId") Long branchId);

    // Tổng tồn cho Product (giữ nguyên)
    @Query("select coalesce(sum(i.qtyOnHand),0) from Inventory i where i.trim.id = :trimId")
    Integer sumQtyByTrim(@Param("trimId") Long trimId);

    @Query("""
        select coalesce(sum(i.qtyOnHand),0)
        from Inventory i
        where i.trim.id = :trimId and i.dealer.id = :dealerId
    """)
    Integer sumQtyByTrimAndDealer(@Param("trimId") Long trimId, @Param("dealerId") Long dealerId);

    // Legacy (giữ cho code cũ)
    List<Inventory> findByLocationTypeOrderByUpdatedAtDesc(String locationType);
    Optional<Inventory> findByTrim_IdAndLocationTypeAndDealer_Id(Long trimId, String locationType, Long dealerId);
    default Optional<Inventory> findByTrimIdAndLocationTypeAndDealerId(Long trimId, String locationType, Long dealerId) {
        return findByTrim_IdAndLocationTypeAndDealer_Id(trimId, locationType, dealerId);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i from Inventory i
        where i.dealer.id = :dealerId and i.trim.id = :trimId
    """)
    Optional<Inventory> lockByDealerAndTrim(@Param("dealerId") Long dealerId,
                                            @Param("trimId") Long trimId);

    // (Khuyến nghị dùng) — lock theo branch + trim
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i from Inventory i
        where i.branch.id = :branchId and i.trim.id = :trimId
    """)
    Optional<Inventory> lockByBranchAndTrim(@Param("branchId") Long branchId,
                                            @Param("trimId") Long trimId);

    /* ===== Tìm/upsert theo branch + trim ===== */
    @Query("""
   select i.trim.id, sum(coalesce(i.qtyOnHand,0) - coalesce(i.reserved,0))
   from Inventory i
   where i.branch.id = :branchId
   group by i.trim.id
""")
    List<Object[]> sumAvailableByTrimAtBranch(@Param("branchId") Long branchId);

    @Query("""
        select i.trim.id, sum(coalesce(i.qtyOnHand,0))
        from Inventory i
        where i.branch.id = :branchId
        group by i.trim.id
    """)
    List<Object[]> sumQtyByTrimAtBranch(@Param("branchId") Long branchId);
}

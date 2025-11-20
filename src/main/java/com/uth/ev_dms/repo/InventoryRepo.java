package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.uth.ev_dms.domain.Trim;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepo extends JpaRepository<Inventory, Long> {

    // ===== THEO DEALER (đang có, giữ nguyên nếu cần) =====
    List<Inventory> findByDealer_Id(Long dealerId);
    List<Inventory> findByDealer_IdOrderByIdDesc(Long dealerId);

    // ===== THEO CHI NHÁNH (BRANCH) =====
    // Lấy toàn bộ kho của 1 chi nhánh
    List<Inventory> findByBranch_Id(Long branchId);

    // Tìm 1 dòng kho theo Trim + Branch (phục vụ upsert)
    Optional<Inventory> findByTrim_IdAndBranch_Id(Long trimId, Long branchId);

    // List có filter tuỳ chọn dealer / branch
    @Query("""
        select i from Inventory i
        where (:dealerId is null or i.dealer.id = :dealerId)
          and (:branchId is null or i.branch.id = :branchId)
        order by i.id desc
    """)
    List<Inventory> findList(@Param("dealerId") Long dealerId,
                             @Param("branchId") Long branchId);

    // ===== TỔNG TỒN THEO TRIM =====
    @Query("select coalesce(sum(i.qtyOnHand),0) from Inventory i where i.trim.id = :trimId")
    Integer sumQtyByTrim(@Param("trimId") Long trimId);

    @Query("""
        select coalesce(sum(i.qtyOnHand),0)
        from Inventory i
        where i.trim.id = :trimId and i.dealer.id = :dealerId
    """)
    Integer sumQtyByTrimAndDealer(@Param("trimId") Long trimId,
                                  @Param("dealerId") Long dealerId);

    // ===== LEGACY (locationType) =====
    List<Inventory> findByLocationTypeOrderByUpdatedAtDesc(String locationType);

    Optional<Inventory> findByTrim_IdAndLocationTypeAndDealer_Id(Long trimId,
                                                                 String locationType,
                                                                 Long dealerId);

    default Optional<Inventory> findByTrimIdAndLocationTypeAndDealerId(Long trimId,
                                                                       String locationType,
                                                                       Long dealerId) {
        return findByTrim_IdAndLocationTypeAndDealer_Id(trimId, locationType, dealerId);
    }

    // ===== LOCK KHO KHI GIAO DỊCH =====
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i from Inventory i
        where i.dealer.id = :dealerId and i.trim.id = :trimId
    """)
    Optional<Inventory> lockByDealerAndTrim(@Param("dealerId") Long dealerId,
                                            @Param("trimId") Long trimId);

    // Lock theo Branch + Trim (nên dùng cho dealer branch)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i from Inventory i
        where i.branch.id = :branchId and i.trim.id = :trimId
    """)
    Optional<Inventory> lockByBranchAndTrim(@Param("branchId") Long branchId,
                                            @Param("trimId") Long trimId);

    // ===== TỔNG TỒN THEO TRIM Ở 1 CHI NHÁNH =====
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

    // ===== TỔNG TỒN CẢ CHI NHÁNH (tính tất cả trim, dùng nếu cần) =====
    @Query("""
        select coalesce(sum(i.qtyOnHand - i.reserved), 0)
        from Inventory i
        where i.branch.id = :branchId
    """)
    int sumAvailableByBranch(@Param("branchId") Long branchId);

    List<Inventory> findByBranchIsNull();   // HQ
    List<Inventory> findByBranchIsNotNull();

    @Query("select sum(i.qtyOnHand) from Inventory i")
    Long sumQty();

    // Tổng tồn kho toàn hệ thống (dùng cho dashboard Admin)
    @Query("select coalesce(sum(i.qtyOnHand), 0) from Inventory i")
    Long sumTotalQty();

    // Chi nhánh
    // ===== LẤY CÁC TRIM CÒN HÀNG THEO DEALER =====
    @Query("""
           select distinct i.trim
           from Inventory i
           where i.dealer.id = :dealerId
             and coalesce(i.qtyOnHand, 0) > 0
           """)
    List<Trim> findAvailableTrimsByDealer(@Param("dealerId") Long dealerId);

    @Transactional
    @Modifying
    @Query("UPDATE Inventory i SET i.qtyOnHand = i.qtyOnHand - :qty " +
            "WHERE i.dealer.id = :dealerId AND i.trim.id = :trimId AND i.qtyOnHand >= :qty")
    int reduceInventory(@Param("dealerId") Long dealerId,
                        @Param("trimId") Long trimId,
                        @Param("qty") Integer qty);




    // === TOTAL INVENTORY BY DEALER ===
    @Query("""
    select coalesce(sum(i.qtyOnHand), 0)
    from Inventory i
    where i.dealer.id = :dealerId""")
    Integer totalByDealer(@Param("dealerId") Long dealerId);


    // === INVENTORY GROUP BY MODEL ===
    @Query("""
    select i.trim.vehicle.modelName, sum(i.qtyOnHand)
    from Inventory i
    where i.dealer.id = :dealerId
    group by i.trim.vehicle.modelName
""")
    List<Object[]> totalGroupByModel(@Param("dealerId") Long dealerId);

    // === LOW STOCK ALERT (< 3 units) ===
    @Query("""
    select i.trim.vehicle.modelName, sum(i.qtyOnHand)
    from Inventory i
    where i.dealer.id = :dealerId
      and i.qtyOnHand < 3
    group by i.trim.vehicle.modelName
""")
    List<Object[]> findLowStockModels(@Param("dealerId") Long dealerId);


}

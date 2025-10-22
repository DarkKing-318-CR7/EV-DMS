package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceListRepo extends JpaRepository<PriceList, Long> {

    // List tất cả giá của 1 trim theo ngày hiệu lực mới → cũ
    List<PriceList> findByTrimIdOrderByEffectiveFromDesc(Long trimId);

    // Lấy *giá đang hiệu lực* cho 1 trim tại 1 ngày (today), ưu tiên effectiveFrom mới nhất
    @Query("""
        select p from PriceList p
        where p.trim.id = :trimId
          and p.active = true
          and (p.effectiveFrom is null or p.effectiveFrom <= :today)
          and (p.effectiveTo   is null or p.effectiveTo   >= :today)
        order by p.effectiveFrom desc, p.id desc
    """)
    List<PriceList> findActiveByTrimAtDate(
            @Param("trimId") Long trimId,
            @Param("today") LocalDate today
    );

    // Nếu bạn muốn lấy *1 bản ghi duy nhất* giá hiện hành:
    @Query("""
        select p from PriceList p
        where p.trim.id = :trimId
          and p.active = true
          and (p.effectiveFrom is null or p.effectiveFrom <= :today)
          and (p.effectiveTo   is null or p.effectiveTo   >= :today)
        order by p.effectiveFrom desc, p.id desc
    """)
    Optional<PriceList> findTopActiveByTrimAtDate(
            @Param("trimId") Long trimId,
            @Param("today") LocalDate today
    );
}

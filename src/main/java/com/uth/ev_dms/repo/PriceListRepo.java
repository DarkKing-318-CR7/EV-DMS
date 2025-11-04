package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceListRepo extends JpaRepository<PriceList, Long> {

    List<PriceList> findByTrimIdOrderByEffectiveFromDesc(Long trimId);

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

    @Query("""
        select p from PriceList p
        join p.trim t
        join t.vehicle v
        where v.modelCode = :modelCode
          and p.active = true
          and (p.effectiveFrom is null or p.effectiveFrom <= :today)
          and (p.effectiveTo   is null or p.effectiveTo   >= :today)
        order by p.effectiveFrom desc, p.id desc
    """)
    Optional<PriceList> findActiveByModelCodeAtDate(
            @Param("modelCode") String modelCode,
            @Param("today") LocalDate today
    );

    List<PriceList> findByTrimId(Long trimId);
}

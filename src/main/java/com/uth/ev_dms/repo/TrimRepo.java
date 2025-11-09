package com.uth.ev_dms.repo;



import com.uth.ev_dms.domain.Trim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrimRepo extends JpaRepository<Trim, Long> {
    List<Trim> findByVehicleId(Long vehicleId);


    // ✅ Custom query: lấy danh sách Trim đang active và có PriceList
    @Query("""
    SELECT DISTINCT t FROM Trim t
    LEFT JOIN FETCH t.vehicle v
    LEFT JOIN FETCH t.priceLists p
    WHERE p.active = true
    """)
    List<Trim> findAllActiveWithPrice();

}


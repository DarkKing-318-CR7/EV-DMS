package com.uth.ev_dms.repo;



import com.uth.ev_dms.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepo extends JpaRepository<Vehicle, Long> {

    // Nên dùng wrapper Long thay vì primitive long (tránh autounbox + đặt tên tham số rõ ràng)
    boolean existsByModelCodeAndIdNot(String modelCode, Long id);

    // (tuỳ chọn) tiện cho validate create
    boolean existsByModelCode(String modelCode);

    @Query("""
   select distinct t.vehicle
   from Inventory i
   join i.trim t
   where i.branch.id = :branchId
     and (coalesce(i.qtyOnHand,0) - coalesce(i.reserved,0)) > 0
   order by t.vehicle.modelName
""")
    List<Vehicle> findVehiclesAvailableAtBranch(@Param("branchId") Long branchId);

}



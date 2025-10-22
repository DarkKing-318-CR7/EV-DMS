package com.uth.ev_dms.repo;



import com.uth.ev_dms.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepo extends JpaRepository<Vehicle, Long> {

    // Nên dùng wrapper Long thay vì primitive long (tránh autounbox + đặt tên tham số rõ ràng)
    boolean existsByModelCodeAndIdNot(String modelCode, Long id);

    // (tuỳ chọn) tiện cho validate create
    boolean existsByModelCode(String modelCode);
}



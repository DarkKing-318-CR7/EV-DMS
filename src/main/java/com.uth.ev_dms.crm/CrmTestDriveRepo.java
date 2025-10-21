package com.uth.ev_dms.crm;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface CrmTestDriveRepo extends JpaRepository<TestDrive, Long> {
    List<TestDrive> findByNgayBetween(LocalDate start, LocalDate end);
}

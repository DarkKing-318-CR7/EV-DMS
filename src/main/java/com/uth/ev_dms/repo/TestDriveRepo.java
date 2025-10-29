package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.TestDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestDriveRepo extends JpaRepository<TestDrive, Long> {
    List<TestDrive> findByStaffId(Long staffId);
}

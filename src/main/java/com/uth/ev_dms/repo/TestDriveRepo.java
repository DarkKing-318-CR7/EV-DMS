package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.domain.TestDriveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveRepo extends JpaRepository<TestDrive, Long> {

    List<TestDrive> findAllByOrderByScheduleAt();

    List<TestDrive> findByStatusOrderByScheduleAt(TestDriveStatus status);

    List<TestDrive> findByScheduleAtBetweenOrderByScheduleAt(LocalDateTime start, LocalDateTime end);

    List<TestDrive> findByScheduleAtBetweenAndStatusOrderByScheduleAt(
            LocalDateTime start, LocalDateTime end, TestDriveStatus status
    );

    List<TestDrive> findByAssignedStaff_IdOrderByScheduleAt(Long staffId);
    List<TestDrive> findByCreatedBy_IdOrderByScheduleAt(Long ownerId);
    boolean existsByVehicleNameAndScheduleAt(String vehicleName, LocalDateTime scheduleAt);
    List<TestDrive> findByCreatedByIdOrderByScheduleAtDesc(Long createdById);
}

package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.domain.TestDriveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveRepo extends JpaRepository<TestDrive, Long> {

    // =========================
    // FILTER LIST
    // =========================
    List<TestDrive> findAllByOrderByScheduleAt();

    List<TestDrive> findByStatusOrderByScheduleAt(TestDriveStatus status);

    List<TestDrive> findByScheduleAtBetweenOrderByScheduleAt(LocalDateTime start, LocalDateTime end);

    List<TestDrive> findByScheduleAtBetweenAndStatusOrderByScheduleAt(
            LocalDateTime start, LocalDateTime end,
            TestDriveStatus status
    );

    // =========================
    // STAFF VIEW / OWNER VIEW
    // =========================
    List<TestDrive> findByAssignedStaff_IdOrderByScheduleAt(Long staffId);

    List<TestDrive> findByCreatedBy_IdOrderByScheduleAt(Long ownerId);

    List<TestDrive> findByCreatedByIdOrderByScheduleAtDesc(Long createdById);

    // =========================
    // CHECK DUPLICATE
    // =========================
    boolean existsByVehicleNameAndScheduleAt(String vehicleName, LocalDateTime scheduleAt);

    // =========================
    // LỊCH HÔM NAY
    // =========================
    @Query("""
        SELECT t
        FROM TestDrive t
        WHERE DATE(t.scheduleAt) = CURRENT_DATE
        ORDER BY t.scheduleAt ASC
    """)
    List<TestDrive> findTodayTestDrives();


    // =========================
    // CHỐNG TRÙNG LỊCH LÁI THỬ
    // =========================
    @Query("""
        SELECT t FROM TestDrive t
        WHERE t.vehicleId = :vehicleId
          AND (
                t.startTime <= :endTime
                AND t.endTime >= :startTime
          )
    """)
    List<TestDrive> findOverlap(Long vehicleId,
                                LocalDateTime startTime,
                                LocalDateTime endTime);

}

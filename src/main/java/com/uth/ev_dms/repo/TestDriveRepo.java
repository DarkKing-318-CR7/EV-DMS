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
    // ⭐ THÊM (CHỐNG TRÙNG LỊCH) — KHÔNG SỬA CODE CŨ
    @org.springframework.data.jpa.repository.Query("""
        SELECT t FROM TestDrive t
        WHERE t.vehicleId = :vehicleId
          AND (
                t.startTime <= :endTime
                AND t.endTime >= :startTime
          )
    """)
    List<TestDrive> findOverlap(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    // ⭐ THÊM MỚI — LỌC THEO ĐẠI LÝ CHO MANAGER
    @org.springframework.data.jpa.repository.Query("""
    SELECT t FROM TestDrive t
    WHERE t.dealer.id = :dealerId
      AND (:status IS NULL OR t.status = :status)
      AND (:from IS NULL OR t.scheduleAt >= :from)
      AND (:to IS NULL OR t.scheduleAt <= :to)
    ORDER BY t.scheduleAt
""")
    List<TestDrive> findByDealerFilter(
            Long dealerId,
            LocalDateTime from,
            LocalDateTime to,
            TestDriveStatus status
    );

}

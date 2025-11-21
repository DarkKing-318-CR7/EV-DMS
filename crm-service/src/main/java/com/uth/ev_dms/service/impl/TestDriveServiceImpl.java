package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.config.CacheConfig;
import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.TestDriveRepo;
import com.uth.ev_dms.repo.CustomerRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.repo.TrimRepo;

import com.uth.ev_dms.service.TestDriveService;
import com.uth.ev_dms.service.dto.TestDriveCreateForm;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestDriveServiceImpl implements TestDriveService {

    private final TestDriveRepo repo;

    private final CustomerRepo customerRepo;
    private final VehicleRepo vehicleRepo;
    private final TrimRepo trimRepo;
    private final TestDriveRepo testDriveRepo;

    // ======================================================
    // ====================== FIND ALL ======================
    // ======================================================

    @Override
    @Cacheable(value = CacheConfig.CacheNames.TESTDRIVES_MANAGER)
    public List<TestDrive> findAll() {
        return repo.findAllByOrderByScheduleAt();
    }

    // ======================================================
    // ================== LIST (MANAGER FILTER) =============
    // ======================================================

    @Override
    @Cacheable(
            value = CacheConfig.CacheNames.TESTDRIVES_MANAGER,
            key = "T(java.util.Objects).hash(#from, #to, #status)"
    )
    public List<TestDrive> list(LocalDate from, LocalDate to, TestDriveStatus status) {

        if (from != null && to != null) {
            LocalDateTime start = from.atStartOfDay();
            LocalDateTime end = to.atTime(LocalTime.MAX);

            return (status != null)
                    ? repo.findByScheduleAtBetweenAndStatusOrderByScheduleAt(start, end, status)
                    : repo.findByScheduleAtBetweenOrderByScheduleAt(start, end);
        }

        if (status != null) return repo.findByStatusOrderByScheduleAt(status);

        return repo.findAllByOrderByScheduleAt();
    }

    // ======================================================
    // ================= FIND MINE (ASSIGNED STAFF) =========
    // ======================================================

    @Override
    @Cacheable(
            value = CacheConfig.CacheNames.TESTDRIVES_BY_OWNER,
            key = "'assigned_' + #staffId"
    )
    public List<TestDrive> findMineAssigned(Long staffId) {
        return repo.findByAssignedStaffIdOrderByScheduleAt(staffId);
    }

    // ======================================================
    // ================= FIND MINE (CREATED BY STAFF) ======
    // ======================================================

    @Override
    @Cacheable(
            value = CacheConfig.CacheNames.TESTDRIVES_BY_OWNER,
            key = "'created_' + #ownerId"
    )
    public List<TestDrive> findMineCreated(Long ownerId) {
        return repo.findByCreatedByIdOrderByScheduleAt(ownerId);
    }

    // ======================================================
    // ===================== FIND BY ID =====================
    // ======================================================

    @Override
    @Cacheable(
            value = CacheConfig.CacheNames.TESTDRIVES_MANAGER,
            key = "'id_' + #id"
    )
    public TestDrive findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy TestDrive ID=" + id));
    }

    @Override
    @Cacheable(
            value = CacheConfig.CacheNames.TESTDRIVES_MANAGER,
            key = "'get_' + #id"
    )
    public TestDrive get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    // ======================================================
    // ====================== APPROVE =======================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.TESTDRIVES_MANAGER,
                    CacheConfig.CacheNames.TESTDRIVES_BY_OWNER
            },
            allEntries = true
    )
    public void approve(Long id) {
        TestDrive td = repo.findById(id).orElseThrow();
        td.setStatus(TestDriveStatus.CONFIRMED);
    }

    // ======================================================
    // ===================== COMPLETE =======================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.TESTDRIVES_MANAGER,
                    CacheConfig.CacheNames.TESTDRIVES_BY_OWNER
            },
            allEntries = true
    )
    public void complete(Long id) {
        TestDrive td = repo.findById(id).orElseThrow();
        td.setStatus(TestDriveStatus.COMPLETED);
    }

    // ======================================================
    // ====================== CANCEL ========================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.TESTDRIVES_MANAGER,
                    CacheConfig.CacheNames.TESTDRIVES_BY_OWNER
            },
            allEntries = true
    )
    public void cancel(Long id) {
        TestDrive td = repo.findById(id).orElseThrow();
        td.setStatus(TestDriveStatus.CANCELLED);
    }

    // ======================================================
    // ======================== SAVE ========================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.TESTDRIVES_MANAGER,
                    CacheConfig.CacheNames.TESTDRIVES_BY_OWNER
            },
            allEntries = true
    )
    public TestDrive save(TestDrive td) {

        if (td.getStatus() == null) {
            td.setStatus(TestDriveStatus.REQUESTED);
        }

        if (repo.existsByVehicleNameAndScheduleAt(td.getVehicleName(), td.getScheduleAt())) {
            throw new RuntimeException("Xe này đã được đặt lịch vào thời gian đó!");
        }

        return repo.save(td);
    }

    // ======================================================
    // ====================== DELETE ========================
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.TESTDRIVES_MANAGER,
                    CacheConfig.CacheNames.TESTDRIVES_BY_OWNER
            },
            allEntries = true
    )
    public void delete(Long id) {
        repo.deleteById(id);
    }

    // ======================================================
    // ====================== CREATE BY STAFF ===============
    // ======================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.TESTDRIVES_MANAGER,
                    CacheConfig.CacheNames.TESTDRIVES_BY_OWNER
            },
            allEntries = true
    )
    public void createByStaff(TestDriveCreateForm form, Long staffId) {

        Customer customer = customerRepo.findById(form.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        Vehicle vehicle = vehicleRepo.findById(form.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Xe không tồn tại"));

        Trim trim = trimRepo.findById(form.getTrimId())
                .orElseThrow(() -> new RuntimeException("Phiên bản không tồn tại"));

        LocalDateTime scheduleAt = LocalDateTime.parse(
                form.getDate() + "T" + form.getTime()
        );

        LocalDateTime startTime = scheduleAt;
        LocalDateTime endTime = scheduleAt.plusMinutes(30);

        // Check overlap
        List<TestDrive> overlaps = testDriveRepo.findOverlap(
                form.getVehicleId(),
                startTime,
                endTime
        );

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Xe này đã bị đặt trong khung giờ này!");
        }

        // ⭐⭐ MICRO-SERVICE: Không còn Dealer entity => chỉ dùng dealerId
        Long dealerId = null;  // bạn có thể thay bằng 1L hoặc lấy từ staff sau này

        TestDrive td = TestDrive.builder()
                .customerName(customer.getTen())
                .customerPhone(customer.getSdt())
                .vehicleName(vehicle.getModelName() + " - " + trim.getTrimName())
                .location(form.getLocation())
                .scheduleAt(scheduleAt)
                .notes(form.getNotes())
                .status(TestDriveStatus.REQUESTED)
                .assignedStaffId(staffId)
                .createdById(staffId)
                .dealerId(dealerId)                // ⭐ CHUẨN MICROSERVICE
                .startTime(startTime)
                .endTime(endTime)
                .build();

        testDriveRepo.save(td);
    }

}

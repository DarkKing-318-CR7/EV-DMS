package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.TestDriveRepo;

// ⭐ THÊM IMPORT — KHÔNG SỬA IMPORT CŨ
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.repo.CustomerRepo;
import com.uth.ev_dms.repo.VehicleRepo;
import com.uth.ev_dms.repo.TrimRepo;

import com.uth.ev_dms.service.TestDriveService;
import com.uth.ev_dms.service.dto.TestDriveCreateForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestDriveServiceImpl implements TestDriveService {

    private final TestDriveRepo repo;

    // ⭐⭐⭐ THÊM 5 REPO — KHÔNG ĐỤNG CODE KHÁC
    private final UserRepo userRepo;
    private final CustomerRepo customerRepo;
    private final VehicleRepo vehicleRepo;
    private final TrimRepo trimRepo;
    private final TestDriveRepo testDriveRepo;

    @Override
    public List<TestDrive> findAll() {
        return repo.findAllByOrderByScheduleAt();
    }

    @Override
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

    @Override
    public List<TestDrive> findMineAssigned(Long staffId) {
        return repo.findByAssignedStaff_IdOrderByScheduleAt(staffId);
    }

    @Override
    public TestDrive findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay TestDrive ID=" + id));
    }

    @Override
    public List<TestDrive> findMineCreated(Long ownerId) {
        return repo.findByCreatedBy_IdOrderByScheduleAt(ownerId);
    }

    @Override
    @Transactional
    public void approve(Long id) {
        TestDrive td = repo.findById(id).orElseThrow();
        td.setStatus(TestDriveStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public void complete(Long id) {
        TestDrive td = repo.findById(id).orElseThrow();
        td.setStatus(TestDriveStatus.COMPLETED);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        TestDrive td = repo.findById(id).orElseThrow();
        td.setStatus(TestDriveStatus.CANCELLED);
    }

    @Override
    @Transactional
    public TestDrive save(TestDrive td) {

        // ⭐ GIỮ NGUYÊN CODE CŨ
        if (td.getStatus() == null) {
            td.setStatus(TestDriveStatus.REQUESTED);
        }

        if (repo.existsByVehicleNameAndScheduleAt(td.getVehicleName(), td.getScheduleAt())) {
            throw new RuntimeException("Xe này đã được đặt lịch vào thời gian đó!");
        }

        return repo.save(td);
    }

    @Override
    public TestDrive get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    @Transactional
    public void createByStaff(TestDriveCreateForm form, Long staffId) {

        // ⭐ GIỮ NGUYÊN TOÀN BỘ LOGIC CŨ

        // Lấy staff
        User staff = userRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff không tồn tại"));

        // Lấy khách hàng
        Customer customer = customerRepo.findById(form.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        // Lấy xe + trim
        Vehicle vehicle = vehicleRepo.findById(form.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Xe không tồn tại"));

        Trim trim = trimRepo.findById(form.getTrimId())
                .orElseThrow(() -> new RuntimeException("Phiên bản không tồn tại"));

        // Gộp ngày + giờ
        LocalDateTime scheduleAt = LocalDateTime.parse(
                form.getDate() + "T" + form.getTime()
        );

        // Tạo entity
        TestDrive td = TestDrive.builder()
                .customerName(customer.getTen())
                .customerPhone(customer.getSdt())
                .vehicleName(vehicle.getModelName() + " - " + trim.getTrimName())
                .location(form.getLocation())
                .scheduleAt(scheduleAt)
                .notes(form.getNotes())
                .status(TestDriveStatus.REQUESTED)
                .assignedStaff(staff)
                .createdBy(staff)
                .dealer(staff.getDealer())
                .build();

        testDriveRepo.save(td);
    }

}

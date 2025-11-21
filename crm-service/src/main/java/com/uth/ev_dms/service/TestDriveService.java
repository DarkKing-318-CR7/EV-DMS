package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.domain.TestDriveStatus;
import com.uth.ev_dms.service.dto.TestDriveCreateForm;

import java.time.LocalDate;
import java.util.List;

public interface TestDriveService {

    // ===== Manager list =====
    List<TestDrive> findAll();
    List<TestDrive> list(LocalDate from, LocalDate to, TestDriveStatus status);
    TestDrive findById(Long id);
    // ===== Staff list =====
    List<TestDrive> findMineAssigned(Long staffId);
    List<TestDrive> findMineCreated(Long ownerId);

    // ===== State changes =====
    void approve(Long id);
    void complete(Long id);
    void cancel(Long id);

    // ===== CRUD =====
    TestDrive save(TestDrive td);
    TestDrive get(Long id);
    void delete(Long id);
    void createByStaff(TestDriveCreateForm form, Long staffId);

}

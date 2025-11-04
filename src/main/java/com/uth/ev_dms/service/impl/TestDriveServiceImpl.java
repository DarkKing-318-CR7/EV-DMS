package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.domain.TestDriveStatus;
import com.uth.ev_dms.repo.TestDriveRepo;
import com.uth.ev_dms.service.TestDriveService;
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
}

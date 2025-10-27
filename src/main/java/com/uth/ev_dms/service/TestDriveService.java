package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.repo.TestDriveRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestDriveService {
    private final TestDriveRepo repo;

    public List<TestDrive> findMine(Long staffId) {
        return repo.findByStaffId(staffId);
    }

    public TestDrive create(TestDrive td) {
        td.setStatus("PENDING");
        return repo.save(td);
    }
}

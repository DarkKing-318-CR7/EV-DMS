package com.uth.ev_dms.service;

import com.uth.ev_dms.repo.CustomerRepo;
import com.uth.ev_dms.repo.OrderItemRepo;
import com.uth.ev_dms.repo.TestDriveRepo;
import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.domain.TestDrive;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffDashboardService {

    private final OrderItemRepo orderItemRepo;
    private final CustomerRepo customerRepo;
    private final TestDriveRepo testDriveRepo;

    public List<Object[]> getHotModelsThisWeek() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return orderItemRepo.findHotModelsThisWeek(weekAgo);
    }

    public List<Customer> latestCustomers() {
        Pageable p = PageRequest.of(0, 5);   // ✔ đúng Pageable
        return customerRepo.findLatestCustomers(p);
    }

    public List<TestDrive> todayTestDrive() {
        return testDriveRepo.findTodayTestDrives();
    }
}

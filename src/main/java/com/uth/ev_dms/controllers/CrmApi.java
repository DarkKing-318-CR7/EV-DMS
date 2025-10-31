package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.domain.TestDriveStatus;
import com.uth.ev_dms.service.TestDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crm")
public class CrmApi {

    private final TestDriveService testDriveService;

    /* ==== LIST (Manager / generic) ==== */
    @GetMapping("/test-drives")
    public List<TestDrive> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(required = false) TestDriveStatus status
    ) {
        return testDriveService.list(from, to, status);
    }

    /* ==== CREATE ==== */
    @PostMapping("/test-drives")
    public TestDrive create(@RequestBody TestDriveCreateDto dto) {
        TestDrive td = new TestDrive();
        td.setCustomerName(dto.getCustomerName());
        td.setCustomerPhone(dto.getCustomerPhone());
        td.setVehicleName(dto.getVehicleName());
        td.setLocation(dto.getLocation());
        td.setScheduleAt(dto.getScheduleAt()); // LocalDateTime
        td.setNotes(dto.getNotes());
        td.setStatus(TestDriveStatus.REQUESTED);
        // Nếu cần gán createdBy/assignedStaff, xử lý ở Service trước khi save
        return testDriveService.save(td);
    }

    /* ==== STATE CHANGES ==== */
    @PostMapping("/test-drives/{id}/approve")
    public void approve(@PathVariable Long id) { testDriveService.approve(id); }

    @PostMapping("/test-drives/{id}/complete")
    public void complete(@PathVariable Long id) { testDriveService.complete(id); }

    @PostMapping("/test-drives/{id}/cancel")
    public void cancel(@PathVariable Long id) { testDriveService.cancel(id); }

    /* ==== DTO tối thiểu cho create ==== */
    public static class TestDriveCreateDto {
        private String customerName;
        private String customerPhone;
        private String vehicleName;
        private String location;
        private LocalDateTime scheduleAt;
        private String notes;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String v) { this.customerName = v; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String v) { this.customerPhone = v; }
        public String getVehicleName() { return vehicleName; }
        public void setVehicleName(String v) { this.vehicleName = v; }
        public String getLocation() { return location; }
        public void setLocation(String v) { this.location = v; }
        public LocalDateTime getScheduleAt() { return scheduleAt; }
        public void setScheduleAt(LocalDateTime v) { this.scheduleAt = v; }
        public String getNotes() { return notes; }
        public void setNotes(String v) { this.notes = v; }
    }
}

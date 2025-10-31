package com.uth.ev_dms.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestDriveCreateDto {
    private String customerName;
    private String customerPhone;
    private String vehicleName;
    private String location;
    private LocalDateTime scheduleAt;
    private String notes;
    private Long assignedStaffId; // optional
}

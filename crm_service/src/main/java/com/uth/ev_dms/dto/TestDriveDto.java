package com.uth.ev_dms.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
public class TestDriveDto {

    private Long id;

    private Long customerId;
    private String customerName;
    private String customerPhone;

    private Long vehicleId;
    private String vehicleName;

    private Long trimId;
    private String trimName;

    private String location;
    private LocalDateTime scheduleTime;
    private String note;

    private String status;
    private Long dealerId;
}

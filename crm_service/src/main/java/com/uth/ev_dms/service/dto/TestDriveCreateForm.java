package com.uth.ev_dms.service.dto;

import lombok.Data;

@Data
public class TestDriveCreateForm {
    private Long customerId;
    private Long vehicleId;
    private Long trimId;

    private String location;
    private String date;
    private String time;
    private String notes;
}

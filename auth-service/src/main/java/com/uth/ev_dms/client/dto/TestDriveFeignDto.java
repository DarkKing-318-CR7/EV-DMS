package com.uth.ev_dms.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestDriveFeignDto {

    private Long id;
    private String status;
    private Long dealerId;

    // Nếu sau này CRM thêm các field khác vào TestDriveDto (customerName, vehicleName,...)
    // bạn có thể bổ sung thêm thuộc tính ở đây cho khớp.
}

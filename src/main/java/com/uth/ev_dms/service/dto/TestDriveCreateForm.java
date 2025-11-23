package com.uth.ev_dms.service.dto;

import lombok.Data;

@Data
public class TestDriveCreateForm {

    // ⭐ CÁC FIELD ĐÃ CÓ THEO MÀN HÌNH STAFF
    private Long customerId;
    private Long vehicleId;
    private Long trimId;
    private String date;      // yyyy-MM-dd
    private String time;      // HH:mm
    private String location;
    private String notes;

    // ⭐⭐⭐ THÊM 3 FIELD BẮT BUỘC ĐỂ GÁN ĐÚNG LỊCH
    private Long dealerId;
    private Long branchId;
    private Long staffId;
}

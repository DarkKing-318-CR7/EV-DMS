package com.uth.ev_dms.domain;

public enum TestDriveStatus {
    REQUESTED,   // Đại lý/staff tạo yêu cầu
    CONFIRMED,   // Manager duyệt lịch
    COMPLETED,   // Đã lái thử
    CANCELLED    // Bị hủy
}

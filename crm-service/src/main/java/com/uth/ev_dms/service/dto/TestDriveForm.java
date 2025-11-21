package com.uth.ev_dms.service.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

public class TestDriveForm {
    private String customerName;
    private String customerPhone;
    private String vehicleName;
    private String location;

    // <input type="datetime-local"> submit theo định dạng yyyy-MM-dd'T'HH:mm
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
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

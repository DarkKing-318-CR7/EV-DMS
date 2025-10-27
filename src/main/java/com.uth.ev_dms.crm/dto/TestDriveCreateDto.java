package com.uth.ev_dms.crm.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class TestDriveCreateDto {
    @NotNull
    private Long customerId;

    @NotNull
    private LocalDate ngay;

    @NotNull
    private LocalTime gio;

    @NotBlank
    private String xe;

    private String ghichu;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }
    public LocalTime getGio() { return gio; }
    public void setGio(LocalTime gio) { this.gio = gio; }
    public String getXe() { return xe; }
    public void setXe(String xe) { this.xe = xe; }
    public String getGhichu() { return ghichu; }
    public void setGhichu(String ghichu) { this.ghichu = ghichu; }
}

package com.uth.ev_dms.crm;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "test_drives")
public class TestDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @NotNull
    private LocalDate ngay;

    @NotNull
    private LocalTime gio;

    @NotBlank
    private String xe;

    @Enumerated(EnumType.STRING)
    private TestDriveStatus trangthai = TestDriveStatus.PENDING;

    private String ghichu;

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public void setNgay(LocalDate ngay) {
        this.ngay = ngay;
    }

    public LocalTime getGio() {
        return gio;
    }

    public void setGio(LocalTime gio) {
        this.gio = gio;
    }

    public String getXe() {
        return xe;
    }

    public void setXe(String xe) {
        this.xe = xe;
    }

    public TestDriveStatus getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(TestDriveStatus trangthai) {
        this.trangthai = trangthai;
    }

    public String getGhichu() {
        return ghichu;
    }

    public void setGhichu(String ghichu) {
        this.ghichu = ghichu;
    }
}

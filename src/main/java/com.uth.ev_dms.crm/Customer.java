package com.uth.ev_dms.crm;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String ten;

    @NotBlank @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9+()\\-\\s]{8,20}$")
    private String sdt;

    private String diachi;

    @NotNull
    private LocalDateTime ngaytao = LocalDateTime.now();

    public Long getId() { return id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getDiachi() { return diachi; }
    public void setDiachi(String diachi) { this.diachi = diachi; }
    public LocalDateTime getNgaytao() { return ngaytao; }
    public void setNgaytao(LocalDateTime ngaytao) { this.ngaytao = ngaytao; }
}

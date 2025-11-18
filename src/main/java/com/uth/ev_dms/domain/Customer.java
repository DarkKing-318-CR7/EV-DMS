package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter @Setter
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ten truong theo thoi quen cua ban
    private String ten;
    private String sdt;
    private String email;
    private String diachi;

    private Long ownerId;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }


    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.ACTIVE;
    @Column(name = "ngaytao")
    private LocalDateTime ngaytao = LocalDateTime.now();

}

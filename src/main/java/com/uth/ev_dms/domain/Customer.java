package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter @Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ten;
    private String sdt;
    private String email;
    private String diachi;

    private Long ownerId;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(name = "ngaytao")
    private LocalDateTime ngaytao = LocalDateTime.now();

    // ⭐⭐ THÊM MỚI – KHÔNG ĐỤNG CODE CŨ
    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;
}

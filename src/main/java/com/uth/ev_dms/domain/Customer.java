package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="customers")
@Getter @Setter
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")    private String ten;
    @Column(name="email")   private String email;
    @Column(name="phone")   private String sdt;
    @Column(name="address") private String diachi;
    @Column(name="owner_id") private Long ownerId;
}


package com.uth.ev_dms.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "role")
@Getter @Setter @NoArgsConstructor
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // ADMIN, EVM, MANAGER, DEALER
}

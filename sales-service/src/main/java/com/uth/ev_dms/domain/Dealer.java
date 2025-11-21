package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="dealers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Dealer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=50)
    private String code;

    @Column(nullable=false, length=150)
    private String name;

    private String region;
    private String phone;
    private String email;

    private String addressLine1;
    private String addressLine2;
    private String ward;
    private String district;
    private String province;

    @Enumerated(EnumType.STRING) @Column(length=16)
    private Status status = Status.ACTIVE;

    @Column(nullable=false, columnDefinition = "bit(1) default 1")
    private boolean active = true;

    private Instant createdAt;
    private Instant updatedAt;

    public enum Status { ACTIVE, INACTIVE }

    @PrePersist void prePersist() {
        createdAt = Instant.now(); updatedAt = createdAt;
    }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}

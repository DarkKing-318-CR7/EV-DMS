package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="dealer_branches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dealer_id"})) // 1:1
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DealerBranch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="dealer_id")
    private Dealer dealer;

    @Column(nullable=false, length=50)
    private String code; // "MAIN"

    @Column(nullable=false, length=150)
    private String name; // "<Dealer name> - Main"

    private String phone;
    private String email;

    private String addressLine1;
    private String addressLine2;
    private String ward;
    private String district;
    private String province;

    @Enumerated(EnumType.STRING) @Column(length=16)
    private Status status = Status.ACTIVE;

    private Instant createdAt;
    private Instant updatedAt;

    public enum Status { ACTIVE, INACTIVE }

    @PrePersist void prePersist() {
        createdAt = Instant.now(); updatedAt = createdAt;
    }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}


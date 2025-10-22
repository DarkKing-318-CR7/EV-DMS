package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "trims")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Trim {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @ToString.Exclude
    private Vehicle vehicle;

    @Column(nullable = false, length = 100)
    private String trimName;

    private Integer batteryKWh;   // kWh
    private Integer rangeKm;      // km
    private Integer powerHp;      // hp

    @Enumerated(EnumType.STRING)
    private DriveType drive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }
}

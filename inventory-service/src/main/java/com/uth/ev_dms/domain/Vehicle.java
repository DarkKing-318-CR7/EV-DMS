package com.uth.ev_dms.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String modelCode;
    private String modelName;
    private String brand;
    private String bodyType;

    // Dùng wrapper để nhận null từ form
    private Integer warrantyMonths;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.warrantyMonths == null) this.warrantyMonths = 0;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    @Setter
    @Getter
    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY)
    private List<Trim> trims = new ArrayList<>();


    @Column(name = "regional_status")
    private String regionalStatus;

    @Column(name = "sales_note")
    private String salesNote;

    @Column(name = "marketing_desc")
    private String marketingDesc;
}

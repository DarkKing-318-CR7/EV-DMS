package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "price_lists")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceList {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trim_id")
    @ToString.Exclude
    private Trim trim;

    @Column(nullable = false, precision = 17, scale = 2)
    private BigDecimal msrp;                 // Giá niêm yết

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private boolean active = true;

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

    @Column(name = "base_price_vnd")
    private Integer basePriceVnd;

    @Column(name = "currency")
    private String currency;
}

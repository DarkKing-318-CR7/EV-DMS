package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_moves")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dealer_id", nullable = false)
    private Long dealerId;

    @Column(name = "trim_id", nullable = false)
    private Long trimId;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false, length = 32)
    private String type;    // RESERVE / RELEASE / SHIP / ADJUST

    @Column(name = "ref_type", length = 32)
    private String refType; // ORDER / MANUAL / etc.

    @Column(name = "ref_id")
    private Long refId;

    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { this.createdAt = LocalDateTime.now(); }

    // ví dụ có Instant createdAt / eventAt ...
    public java.time.LocalDateTime getCreatedAtLdt() {
        return createdAt == null ? null
                : createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }

}

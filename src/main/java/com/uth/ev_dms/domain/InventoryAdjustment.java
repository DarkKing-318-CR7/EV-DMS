package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_adjustments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // kho nào bị chỉnh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    // +10, -3 ...
    @Column(name = "delta_qty")
    private Integer deltaQty;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void touch() {
        this.createdAt = LocalDateTime.now();
    }
}

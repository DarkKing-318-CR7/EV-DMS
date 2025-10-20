package com.uth.ev_dms.domain;


import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.Instant;

@Entity
@Table(name = "inventory_adjustments",
        indexes = {@Index(columnList = "createdAt"), @Index(columnList = "reason")})
@Getter @Setter @NoArgsConstructor
public class InventoryAdjustment extends BaseAudit {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @NotNull
    @Column(nullable = false)
    private Integer deltaQty;              // +10 allocate, -5 recall...

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private AdjReason reason = AdjReason.ADJUST;

    @Column(nullable = false, updatable = false)
    private Instant createdAtEvent = Instant.now(); // thời điểm ghi nhận sự kiện (khác createdAt entity khi cần)
}

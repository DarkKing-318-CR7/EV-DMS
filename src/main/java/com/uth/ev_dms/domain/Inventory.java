package com.uth.ev_dms.domain;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "inventories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dealer_id", "trim_id"}),
        indexes = {@Index(columnList = "dealer_id"), @Index(columnList = "trim_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseAudit {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trim_id", nullable = false)
    private Trim trim;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer quantity = 0;    // Tồn thực tế (on-hand)

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer reserved = 0;    // Tồn đang giữ cho đơn hàng
}

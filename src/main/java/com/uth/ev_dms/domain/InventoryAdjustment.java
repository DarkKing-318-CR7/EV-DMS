package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_adjustments")
@Getter
@Setter
public class InventoryAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // quan hệ tới inventory cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    // số lượng thay đổi (+5, -2, v.v.)
    @Column(name = "delta_qty", nullable = false)
    private Integer deltaQty;

    // lý do chỉnh kho
    @Column(name = "reason", length = 255)
    private String reason;

    // thời điểm ghi nhận event (có vẻ là thời điểm thực tế thay đổi stock)
    @Column(name = "created_at_event")
    private LocalDateTime createdAtEvent;

    // audit chung
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;
}

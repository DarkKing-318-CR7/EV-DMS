package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventories",
        // ✅ unique theo chi nhánh + trim
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "trim_id"}),
        indexes = {
                @Index(columnList = "dealer_id"),
                @Index(columnList = "branch_id"),
                @Index(columnList = "trim_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==== Audit fields (giữ nguyên) ====
    @CreationTimestamp @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name="updated_at") private LocalDateTime updatedAt;
    @Column(name="created_by") private String createdBy;
    @Column(name="updated_by") private String updatedBy;

    // ==== Quan hệ bắt buộc ====
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="dealer_id")
    private Dealer dealer;

    // ✅ THÊM: Chi nhánh (MAIN hiện tại)
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="branch_id")
    private DealerBranch branch;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="trim_id")
    private Trim trim;

    // ==== Fields kho (giữ nguyên) ====
    @Column(name="location_type") private String locationType;
    @Column(name="qty_on_hand")  private Integer qtyOnHand;

    @NotNull @Min(0) @Column(name="quantity", nullable=false)
    private Integer quantity = 0;

    @NotNull @Min(0) @Column(name="quantity_reserved", nullable=false)
    private Integer reserved = 0;

    // ==== Defaults ====
    @PrePersist public void preInsertDefaults() {
        if (locationType == null || locationType.isBlank()) locationType = "EVM";
        if (qtyOnHand == null) qtyOnHand = 0;
        if (quantity == null) quantity = qtyOnHand;
        if (reserved == null) reserved = 0;
    }
    @PreUpdate public void preUpdateDefaults() {
        if (locationType == null || locationType.isBlank()) locationType = "EVM";
        if (qtyOnHand == null) qtyOnHand = 0;
        if (quantity == null) quantity = qtyOnHand;
        if (reserved == null) reserved = 0;
    }
}

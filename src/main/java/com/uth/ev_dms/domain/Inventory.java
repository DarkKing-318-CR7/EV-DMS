package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "inventories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "trim_id"}),
        indexes = {
                @Index(columnList = "dealer_id"),
                @Index(columnList = "branch_id"),
                @Index(columnList = "trim_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Audit =====
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="created_by")
    private String createdBy;

    @Column(name="updated_by")
    private String updatedBy;

    // ===== Quan hệ bắt buộc =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="dealer_id", nullable = false)
    private Dealer dealer;

    // ⭐ Cho phép NULL = kho tổng (HQ)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="branch_id")
    private DealerBranch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="trim_id", nullable = false)
    private Trim trim;

    // ===== Số lượng kho =====
    @Column(name="location_type")
    private String locationType;     // HQ hoặc BRANCH

    @Column(name="qty_on_hand")
    private Integer qtyOnHand = 0;   // tồn vật lý

    @Column(name="reserved")
    private Integer reserved = 0;    // số đã giữ

    // ==== Defaults ====
    @PrePersist
    public void prePersist() {
        if (locationType == null || locationType.isBlank()) {
            locationType = (branch == null) ? "HQ" : "BRANCH";
        }
        if (qtyOnHand == null) qtyOnHand = 0;
        if (reserved == null) reserved = 0;
    }

    @PreUpdate
    public void preUpdate() {
        if (locationType == null || locationType.isBlank()) {
            locationType = (branch == null) ? "HQ" : "BRANCH";
        }
        if (qtyOnHand == null) qtyOnHand = 0;
        if (reserved == null) reserved = 0;
    }

    List<Inventory> findByBranchIsNull() {
        return null;
    }

}

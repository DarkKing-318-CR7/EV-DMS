package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
        name = "inventories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dealer_id", "trim_id"}),
        indexes = {
                @Index(columnList = "dealer_id"),
                @Index(columnList = "trim_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseAudit {

    // ==== Quan hệ bắt buộc ====
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trim_id", nullable = false)
    private Trim trim;

    // ==== Thuộc tính kho ====
    // Loại vị trí: "EVM" (kho tổng), sau này có thể "DEALER"
    @Column(name = "location_type")
    private String locationType;

    // Số lượng thực tế đang có (on-hand)
    @Column(name = "qty_on_hand")
    private Integer qtyOnHand;

    // Số lượng khả dụng (cho bán) - không âm
    @NotNull
    @Min(0)
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    // Số lượng đã giữ chỗ cho đơn - không âm
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer reserved = 0;

    // ==== Lifecycle hooks: đảm bảo giá trị mặc định hợp lệ ====
    @PrePersist
    public void preInsertDefaults() {
        if (locationType == null || locationType.isBlank()) {
            locationType = "EVM";
        }
        if (qtyOnHand == null) {
            qtyOnHand = 0;
        }
        if (quantity == null) {
            // rule: quantity = qtyOnHand lúc tạo lần đầu
            quantity = qtyOnHand;
        }
        if (reserved == null) {
            reserved = 0;
        }
    }

    @PreUpdate
    public void preUpdateDefaults() {
        if (locationType == null || locationType.isBlank()) {
            locationType = "EVM";
        }
        if (qtyOnHand == null) {
            qtyOnHand = 0;
        }
        if (quantity == null) {
            quantity = qtyOnHand;
        }
        if (reserved == null) {
            reserved = 0;
        }
    }
    // --- Backward-compat for legacy code calling setUpdatedAt(LocalDateTime) ---
    public void setUpdatedAt(java.time.LocalDateTime t) {
        if (t == null) {
            // tuỳ bạn muốn xử lý null thế nào; ở đây mình bỏ qua
            return;
        }
        java.time.Instant instant = t.atZone(java.time.ZoneId.systemDefault()).toInstant();
        // gọi phương thức của BaseAudit (kiểu Instant)
        super.setUpdatedAt(instant);
    }

}

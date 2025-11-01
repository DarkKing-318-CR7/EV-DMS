package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Trim cụ thể (ví dụ: EV6 GT-Line AWD Long Range)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trim_id")
    private Trim trim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    // Loại vị trí: "EVM" (kho tổng), sau này có thể "DEALER"
    @Column(name = "location_type")
    private String locationType;

    // Số lượng hiện có
    @Column(name = "qty_on_hand")
    private Integer qtyOnHand;

    @Column(name = "quantity")
    private Integer quantity;

    // Audit nhẹ
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @PrePersist
    public void onInsert() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (locationType == null || locationType.isBlank()) {
            this.locationType = "EVM";
        }

        if (qtyOnHand == null) {
            this.qtyOnHand = 0;
        }

        // đảm bảo 'quantity' cũng có giá trị hợp lệ khi insert
        if (quantity == null) {
            // rule: bạn muốn quantity = qtyOnHand lúc tạo lần đầu?
            this.quantity = this.qtyOnHand;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (locationType == null || locationType.isBlank()) {
            this.locationType = "EVM";
        }
        if (qtyOnHand == null) {
            this.qtyOnHand = 0;
        }
        if (quantity == null) {
            this.quantity = this.qtyOnHand;
        }
    }

}

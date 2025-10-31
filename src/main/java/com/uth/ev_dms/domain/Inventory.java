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

    // Loại vị trí: "EVM" (kho tổng), sau này có thể "DEALER"
    @Column(name = "location_type")
    private String locationType;

    // Số lượng hiện có
    @Column(name = "qty_on_hand")
    private Integer qtyOnHand;

    // Audit nhẹ
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touchTs() {
        this.updatedAt = LocalDateTime.now();
        if (locationType == null || locationType.isBlank()) {
            this.locationType = "EVM";
        }
        if (qtyOnHand == null) {
            this.qtyOnHand = 0;
        }
    }
}

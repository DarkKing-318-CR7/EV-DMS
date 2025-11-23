package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.uth.ev_dms.domain.DealerBranch;

@Entity
@Table(name = "inventories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trim_id")
    private Trim trim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private DealerBranch branch;

    @Column(name = "location_type")
    private String locationType;

    @Column(name = "quantity")      // ✔ DB column là quantity
    private Integer qtyOnHand;      // ✔ Field tên qtyOnHand

    @Column(name = "reserved")
    private Integer reserved;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.reserved == null) this.reserved = 0;   // optional, tránh null
        if (this.qtyOnHand == null) this.qtyOnHand = 0; // optional
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

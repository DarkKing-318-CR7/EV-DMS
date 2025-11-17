package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Optional;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên Trim (tính toán ở Controller)
    @Transient
    private String trimName;

    public String getTrimName() { return trimName; }
    public void setTrimName(String trimName) { this.trimName = trimName; }

    // =========================
    //  CÁC TRƯỜNG MAP DATABASE
    // =========================

    @Column(name = "vehicle_id")
    private Long vehicleId;



    @Column(name = "trim_id")   // ⭐ RẤT QUAN TRỌNG: tránh trùng trimId/trim_id
    private Long trimId;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderHdr order;

    private Integer qty;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "line_amount", nullable = false)
    private BigDecimal lineAmount;

    // =========================
    //  GETTERS / SETTERS
    // =========================

    public Long getTrimId() { return trimId; }
    public void setTrimId(Long trimId) { this.trimId = trimId; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getLineAmount() { return lineAmount; }
    public void setLineAmount(BigDecimal lineAmount) { this.lineAmount = lineAmount; }

    public OrderHdr getOrder() { return order; }
    public void setOrder(OrderHdr order) { this.order = order; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // =========================
    //  TÍNH TOÁN
    // =========================
    public BigDecimal getSubtotal() {
        var price = Optional.ofNullable(unitPrice).orElse(BigDecimal.ZERO);
        var qtyVal = BigDecimal.valueOf(Optional.ofNullable(qty).orElse(0));
        var disc = Optional.ofNullable(discountAmount).orElse(BigDecimal.ZERO);
        return price.multiply(qtyVal).subtract(disc);
    }
}

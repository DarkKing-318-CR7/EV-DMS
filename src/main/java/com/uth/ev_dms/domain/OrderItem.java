package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Optional;

@Entity @Table(name = "order_item")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Transient
    private String trimName;                  // <-- thêm

    public String getTrimName() { return trimName; }
    public void setTrimName(String trimName) { this.trimName = trimName; }

    private Long vehicleId;
    private Long trimId;
    private String color;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id")
    private OrderHdr order;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal lineAmount;



    public Long getTrimId() { return trimId; }
    public void setTrimId(Long trimId) { this.trimId = trimId; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public java.math.BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public java.math.BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(java.math.BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public OrderHdr getOrder() { return order; }
    public void setOrder(OrderHdr order) { this.order = order; }
    public BigDecimal getSubtotal() {
        var price = Optional.ofNullable(unitPrice).orElse(BigDecimal.ZERO);
        var qtyVal = BigDecimal.valueOf(Optional.ofNullable(qty).orElse(0));
        var disc = Optional.ofNullable(discountAmount).orElse(BigDecimal.ZERO);
        return price.multiply(qtyVal).subtract(disc);
    }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public BigDecimal getLineAmount() { return lineAmount; }
    public void setLineAmount(BigDecimal lineAmount) { this.lineAmount = lineAmount; }






    // getters/setters
}

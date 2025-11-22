package com.uth.ev_dms.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderHdr order;
    private String method;          // CASH/CARD/TRANSFER/INSTALLMENT
    private BigDecimal amount;
    private LocalDateTime paidAt = LocalDateTime.now();
    private String note;

    @Enumerated(EnumType.STRING)
    private PaymentType type;
    private String refNo;


    public Long getId() {return id;}

    public OrderHdr getOrder() {return order;}

    public void setOrder(OrderHdr order) {this.order = order;}

    public PaymentType getType() {return type;}

    public void setType(PaymentType type) {this.type = type;}

    public BigDecimal getAmount() {return amount;}

    public void setAmount(BigDecimal amount) {this.amount = amount;}

    public String getMethod() {return method;}

    public void setMethod(String method) {this.method = method;}

    public String getRefNo() {return refNo;}

    public void setRefNo(String refNo) {this.refNo = refNo;}

    public LocalDateTime getPaidAt() {return paidAt;}

    public void setPaidAt(LocalDateTime paidAt) {this.paidAt = paidAt;}

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

}

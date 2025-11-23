package com.uth.ev_dms.service.dto;

import java.math.BigDecimal;

public class PaymentRequestDTO {
    private BigDecimal amount;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

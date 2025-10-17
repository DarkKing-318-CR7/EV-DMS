package com.ev.dms.application.sales.dto;

import java.math.BigDecimal;

public class PaymentRequestDTO {
    private BigDecimal amount;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

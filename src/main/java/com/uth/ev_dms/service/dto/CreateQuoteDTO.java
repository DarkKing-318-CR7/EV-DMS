package com.uth.ev_dms.service.dto;

import java.math.BigDecimal;
import java.util.List;

public class CreateQuoteDTO {
    private Long customerId;
    private BigDecimal totalAmount; // có thể tự tính từ items nếu muốn
    private List<CreateQuoteItemDTO> items;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public List<CreateQuoteItemDTO> getItems() { return items; }
    public void setItems(List<CreateQuoteItemDTO> items) { this.items = items; }
}

package com.uth.ev_dms.sales.service;

import com.uth.ev_dms.sales.domain.*;
import com.uth.ev_dms.sales.service.dto.CreateQuoteDTO;

import java.math.BigDecimal;

public interface SalesService {
    Quote createQuote(CreateQuoteDTO dto);
    OrderHdr approveQuote(Long quoteId);
    Payment makeCashPayment(Long orderId, BigDecimal amount);
    Payment makeInstallmentPayment(Long orderId, BigDecimal amount); // placeholder
}

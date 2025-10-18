package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;

import java.math.BigDecimal;

public interface SalesService {
    Quote createQuote(CreateQuoteDTO dto);
    OrderHdr approveQuote(Long quoteId);
    Payment makeCashPayment(Long orderId, BigDecimal amount);
    Payment makeInstallmentPayment(Long orderId, BigDecimal amount); // placeholder
}

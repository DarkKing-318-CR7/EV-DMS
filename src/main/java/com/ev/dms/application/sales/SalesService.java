package com.ev.dms.application.sales;

import com.ev.dms.domain.sales.OrderHdr;
import com.ev.dms.domain.sales.Payment;
import com.ev.dms.domain.sales.Quote;
import com.uth.ev_dms.sales.domain.*;
import com.ev.dms.application.sales.dto.CreateQuoteDTO;

import java.math.BigDecimal;

public interface SalesService {
    Quote createQuote(CreateQuoteDTO dto);
    OrderHdr approveQuote(Long quoteId);
    Payment makeCashPayment(Long orderId, BigDecimal amount);
    Payment makeInstallmentPayment(Long orderId, BigDecimal amount); // placeholder
}

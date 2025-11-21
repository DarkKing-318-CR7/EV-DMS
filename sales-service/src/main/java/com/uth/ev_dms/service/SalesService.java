package com.uth.ev_dms.service;

import java.util.List;
import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.domain.Quote;
import com.uth.ev_dms.service.dto.CreateQuoteDTO;

import java.math.BigDecimal;

public interface SalesService {
    // === Quote Workflow ===
    Quote createQuote(CreateQuoteDTO dto);

    // Manager phê duyệt báo giá
    OrderHdr approveQuote(Long quoteId);

    // Staff gửi duyệt, Manager từ chối
    Quote submitQuote(Long quoteId);
    Quote rejectQuote(Long quoteId, String comment);

    // Danh sách quote
    List<Quote> findPending();   // cho Manager
    List<Quote> findAll();       // cho Staff (hoặc findByDealerId)

    // === Promotions ===
    Quote applyPromotions(Long quoteId, List<Long> promotionIds);

    // === Payment ===
    Payment makeCashPayment(Long orderId, BigDecimal amount);
    Payment makeInstallmentPayment(Long orderId, BigDecimal amount);
}

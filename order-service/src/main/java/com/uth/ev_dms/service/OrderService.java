package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;

import java.util.List;

public interface OrderService {

    // ===== READ =====
    OrderHdr findById(Long id);

    List<OrderItem> findItems(Long id);

    List<OrderHdr> findMine(Long staffId);

    List<OrderHdr> findAllForDealer(Long dealerId);

    // ===== CREATE =====
    OrderHdr createFromQuote(Long quoteId, Long dealerIdIgnored, Long customerIdIgnored, Long staffIdIgnored);

    // ===== STATUS FLOW =====
    OrderHdr submitForAllocation(Long orderId);

    OrderHdr allocate(Long orderId);

    OrderHdr markDelivered(Long orderId);

    // ===== CANCEL =====
    OrderHdr cancel(Long orderId);

    OrderHdr cancelByDealer(Long orderId, Long dealerId, Long actorId);

    // ===== DEALLOCATE + CANCEL BY EVM =====
    OrderHdr deallocateByEvm(Long orderId, Long actorId, String reason);

    OrderHdr cancelByEvm(Long orderId, Long actorId, String reason);
}

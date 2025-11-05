package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;

import java.util.List;

public interface OrderService {
    OrderHdr createFromQuote(Long quoteId, Long dealerId, Long customerId, Long staffId);
    OrderHdr submitForAllocation(Long orderId);
    OrderHdr allocate(Long orderId);
    OrderHdr markDelivered(Long orderId);
    List<OrderHdr> findMine(Long staffId);
    List<OrderHdr> findAllForDealer(Long dealerId);
    OrderHdr findById(Long id);
    List<OrderItem> findItems(Long id);



    // ✅ EVM thu hồi phân bổ (ALLOCATED -> PENDING_ALLOC)
    OrderHdr deallocateByEvm(Long orderId, Long actorId, String reason);

    // ✅ EVM hủy (cho NEW, PENDING_ALLOC; hoặc sau khi đã deallocate)
    OrderHdr cancelByEvm(Long orderId, Long actorId, String reason);
    OrderHdr cancelByDealer(Long id, Long dealerId, Long actorId);

}


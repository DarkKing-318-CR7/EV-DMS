package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.InventoryService;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Primary  // ✅ Ưu tiên bản này khi có nhiều bean cùng kiểu
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepo inventoryRepo;
    private final InventoryMoveRepo moveRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;

    @PersistenceContext
    private EntityManager em;

    // ========= Allocate cho từng item =========
    @Override
    @Transactional
    public boolean allocateForOrder(OrderItem item) {
        OrderHdr order = item.getOrder();
        if (order == null || order.getDealerId() == null)
            throw new IllegalStateException("Order or dealerId not found for item " + item.getId());

        Long dealerId = order.getDealerId();
        Long trimId = resolveTrimId(item);
        int qty = item.getQty() != null ? item.getQty() : 0;
        if (qty <= 0) return true;

        Inventory inv = inventoryRepo.lockByDealerAndTrim(dealerId, trimId)
                .orElseGet(() -> inventoryRepo.save(
                        Inventory.builder()
                                .dealer(em.getReference(Dealer.class, dealerId))
                                .trim(em.getReference(Trim.class, trimId))
                                .quantity(0)
                                .reserved(0)
                                .build()
                ));

        int avail = inv.getQuantity() - inv.getReserved();
        if (avail < qty) return false;

        inv.setReserved(inv.getReserved() + qty);
        inventoryRepo.save(inv);

        moveRepo.save(InventoryMove.builder()
                .dealerId(dealerId)
                .trimId(trimId)
                .qty(qty)
                .type("RESERVE")
                .refType("ORDER")
                .refId(item.getId())
                .note("Reserve for orderItem " + item.getId())
                .build());

        return true;
    }

    // ========= Allocate cho cả đơn =========
    @Override
    @Transactional
    public void allocateForOrder(Long orderId) {
        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        if (items == null || items.isEmpty())
            throw new IllegalStateException("Order has no items");

        Long dealerId = order.getDealerId();

        for (OrderItem it : items) {
            Long trimId = resolveTrimId(it);
            int need = it.getQty() != null ? it.getQty() : 0;

            Inventory inv = inventoryRepo.lockByDealerAndTrim(dealerId, trimId)
                    .orElseGet(() -> inventoryRepo.save(
                            Inventory.builder()
                                    .dealer(em.getReference(Dealer.class, dealerId))
                                    .trim(em.getReference(Trim.class, trimId))
                                    .quantity(0)
                                    .reserved(0)
                                    .build()
                    ));

            int avail = inv.getQuantity() - inv.getReserved();
            if (avail < need)
                throw new IllegalStateException("Out of stock for trim=" + trimId);
        }

        // Reserve all
        for (OrderItem it : items) {
            if (!allocateForOrder(it))
                throw new IllegalStateException("Allocation failed for item " + it.getId());
        }
    }

    // ========= Ship =========
    @Override
    @Transactional
    public void shipForOrder(Long orderId) {
        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        Long dealerId = order.getDealerId();

        for (OrderItem it : items) {
            Long trimId = resolveTrimId(it);
            int qty = it.getQty() != null ? it.getQty() : 0;
            if (qty <= 0) continue;

            Inventory inv = inventoryRepo.lockByDealerAndTrim(dealerId, trimId)
                    .orElseThrow(() -> new IllegalStateException("Inventory not found"));

            if (inv.getReserved() < qty || inv.getQuantity() < qty)
                throw new IllegalStateException("Invalid inventory to ship for trim=" + trimId);

            inv.setReserved(inv.getReserved() - qty);
            inv.setQuantity(inv.getQuantity() - qty);
            inventoryRepo.save(inv);

            moveRepo.save(InventoryMove.builder()
                    .dealerId(dealerId)
                    .trimId(trimId)
                    .qty(qty)
                    .type("SHIP")
                    .refType("ORDER")
                    .refId(it.getId())
                    .note("Ship for orderItem " + it.getId())
                    .build());
        }
    }

    // ========= Release =========
    @Override
    @Transactional
    public void releaseForOrder(Long orderId) {
        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        Long dealerId = order.getDealerId();

        for (OrderItem it : items) {
            Long trimId = resolveTrimId(it);
            int qty = it.getQty() != null ? it.getQty() : 0;
            if (qty <= 0) continue;

            Inventory inv = inventoryRepo.lockByDealerAndTrim(dealerId, trimId)
                    .orElseGet(() -> inventoryRepo.save(
                            Inventory.builder()
                                    .dealer(em.getReference(Dealer.class, dealerId))
                                    .trim(em.getReference(Trim.class, trimId))
                                    .quantity(0)
                                    .reserved(0)
                                    .build()
                    ));

            inv.setReserved(Math.max(0, inv.getReserved() - qty));
            inventoryRepo.save(inv);

            moveRepo.save(InventoryMove.builder()
                    .dealerId(dealerId)
                    .trimId(trimId)
                    .qty(qty)
                    .type("RELEASE")
                    .refType("ORDER")
                    .refId(it.getId())
                    .note("Release for orderItem " + it.getId())
                    .build());
        }
    }

    // ===== Helper =====
    private Long resolveTrimId(OrderItem item) {
        if (item.getTrimId() != null) return item.getTrimId();
        if (item.getVehicleId() != null) return item.getVehicleId();
        throw new IllegalStateException("No trim or vehicle id on order item " + item.getId());
    }
}

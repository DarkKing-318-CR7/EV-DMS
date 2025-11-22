package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.config.CacheConfig;
import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.InventoryService;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
import com.uth.ev_dms.service.vm.NifiService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Primary
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepo inventoryRepo;
    private final InventoryMoveRepo moveRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final DealerBranchRepo dealerBranchRepo;
    private final InventoryAdjustmentRepo inventoryAdjustmentRepo;

    private final NifiService nifiService;   // ⬅️ NiFi ADDED

    @PersistenceContext
    private EntityManager em;

    // ============================================================
    // =============== ORDER FLOW FUNCTIONS (WRITE) ================
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(value = {
            CacheConfig.CacheNames.INVENTORY_BY_BRANCH,
            CacheConfig.CacheNames.INVENTORY_BY_DEALER,
            CacheConfig.CacheNames.INVENTORY_LIST
    }, allEntries = true)
    public boolean allocateForOrder(OrderItem item) {

        OrderHdr order = item.getOrder();
        if (order == null || order.getDealerId() == null) {
            throw new IllegalStateException("Order or dealerId not found for item " + item.getId());
        }

        final Long dealerId = order.getDealerId();
        final Long trimId = resolveTrimId(item);
        final int qty = item.getQty() != null ? item.getQty() : 0;
        if (qty <= 0) return true;

        final Long branchId = dealerBranchRepo.findByDealerId(dealerId)
                .orElseThrow(() -> new IllegalStateException("Dealer has no MAIN branch"))
                .getId();

        Inventory inv = inventoryRepo.lockByBranchAndTrim(branchId, trimId)
                .orElseGet(() ->
                        Inventory.builder()
                                .dealer(em.getReference(Dealer.class, dealerId))
                                .branch(em.getReference(DealerBranch.class, branchId))
                                .trim(em.getReference(Trim.class, trimId))
                                .qtyOnHand(0)
                                .reserved(0)
                                .build()
                );

        int onHand = Optional.ofNullable(inv.getQtyOnHand()).orElse(0);
        int reserved = Optional.ofNullable(inv.getReserved()).orElse(0);
        int avail = onHand - reserved;

        if (avail < qty) return false;

        inv.setReserved(reserved + qty);
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

        // ⬅️ Push NiFi event
        nifiService.sendToNifi(inv);

        return true;
    }


    @Override
    @Transactional
    @CacheEvict(value = {
            CacheConfig.CacheNames.INVENTORY_BY_BRANCH,
            CacheConfig.CacheNames.INVENTORY_BY_DEALER,
            CacheConfig.CacheNames.INVENTORY_LIST
    }, allEntries = true)
    public void allocateForOrder(Long orderId) {

        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Order has no items");
        }

        final Long dealerId = order.getDealerId();
        final Long branchId = dealerBranchRepo.findByDealerId(dealerId)
                .orElseThrow(() -> new IllegalStateException("Dealer has no MAIN branch"))
                .getId();

        // CHECK trước
        for (OrderItem it : items) {

            Long trimId = resolveTrimId(it);
            int need = it.getQty() != null ? it.getQty() : 0;

            Inventory inv = inventoryRepo.lockByBranchAndTrim(branchId, trimId)
                    .orElseGet(() ->
                            Inventory.builder()
                                    .dealer(em.getReference(Dealer.class, dealerId))
                                    .branch(em.getReference(DealerBranch.class, branchId))
                                    .trim(em.getReference(Trim.class, trimId))
                                    .qtyOnHand(0)
                                    .reserved(0)
                                    .build()
                    );

            int onHand = Optional.ofNullable(inv.getQtyOnHand()).orElse(0);
            int reserved = Optional.ofNullable(inv.getReserved()).orElse(0);
            int avail = onHand - reserved;

            if (avail < need) {
                throw new IllegalStateException("Out of stock for trim=" + trimId);
            }
        }

        // RESERVE
        for (OrderItem it : items) {
            if (!allocateForOrder(it)) {
                throw new IllegalStateException("Allocation failed for item " + it.getId());
            }
        }
    }


    @Override
    @Transactional
    @CacheEvict(value = {
            CacheConfig.CacheNames.INVENTORY_BY_BRANCH,
            CacheConfig.CacheNames.INVENTORY_BY_DEALER,
            CacheConfig.CacheNames.INVENTORY_LIST
    }, allEntries = true)
    public void shipForOrder(Long orderId) {

        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        final Long dealerId = order.getDealerId();
        final Long branchId = dealerBranchRepo.findByDealerId(dealerId)
                .orElseThrow(() -> new IllegalStateException("Dealer has no MAIN branch"))
                .getId();

        for (OrderItem it : items) {

            Long trimId = resolveTrimId(it);
            int qty = Optional.ofNullable(it.getQty()).orElse(0);
            if (qty <= 0) continue;

            Inventory inv = inventoryRepo.lockByBranchAndTrim(branchId, trimId)
                    .orElseThrow(() ->
                            new IllegalStateException("Inventory not found (branch=" + branchId + ", trim=" + trimId + ")")
                    );

            int reserved = Optional.ofNullable(inv.getReserved()).orElse(0);
            int onHand = Optional.ofNullable(inv.getQtyOnHand()).orElse(0);

            if (reserved < qty || onHand < qty) {
                throw new IllegalStateException("Invalid inventory to ship for trim=" + trimId);
            }

            inv.setReserved(reserved - qty);
            inv.setQtyOnHand(onHand - qty);

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

            // ⬅️ Push NiFi event
            nifiService.sendToNifi(inv);
        }
    }


    @Override
    @Transactional
    @CacheEvict(value = {
            CacheConfig.CacheNames.INVENTORY_BY_BRANCH,
            CacheConfig.CacheNames.INVENTORY_BY_DEALER,
            CacheConfig.CacheNames.INVENTORY_LIST
    }, allEntries = true)
    public void releaseForOrder(Long orderId) {

        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        final Long dealerId = order.getDealerId();
        final Long branchId = dealerBranchRepo.findByDealerId(dealerId)
                .orElseThrow(() -> new IllegalStateException("Dealer has no MAIN branch"))
                .getId();

        for (OrderItem it : items) {

            Long trimId = resolveTrimId(it);
            int qty = Optional.ofNullable(it.getQty()).orElse(0);
            if (qty <= 0) continue;

            Inventory inv = inventoryRepo.lockByBranchAndTrim(branchId, trimId)
                    .orElseThrow(() ->
                            new IllegalStateException("Inventory not found (branch=" + branchId + ", trim=" + trimId + ")")
                    );

            int reserved = Optional.ofNullable(inv.getReserved()).orElse(0);

            inv.setReserved(Math.max(0, reserved - qty));
            inventoryRepo.save(inv);

            moveRepo.save(InventoryMove.builder()
                    .dealerId(dealerId)
                    .trimId(trimId)
                    .qty(qty)
                    .type("RELEASE")
                    .refType("ORDER")
                    .refId(it.getId())
                    .note("Release reserved for orderItem " + it.getId())
                    .build());

            // ⬅️ Push NiFi event
            nifiService.sendToNifi(inv);
        }
    }

    // ============================================================
    // ======================= ADMIN INVENTORY =====================
    // ============================================================

    @Override
    @Cacheable(value = CacheConfig.CacheNames.INVENTORY_LIST)
    public List<Inventory> findAll() {
        return inventoryRepo.findAll();
    }

    @Override
    @Cacheable(value = CacheConfig.CacheNames.INVENTORY_ONE, key = "#id")
    public Optional<Inventory> findById(Long id) {
        return inventoryRepo.findById(id);
    }

    @Override
    @CacheEvict(value = {
            CacheConfig.CacheNames.INVENTORY_LIST,
            CacheConfig.CacheNames.INVENTORY_ONE,
            CacheConfig.CacheNames.INVENTORY_BY_BRANCH,
            CacheConfig.CacheNames.INVENTORY_BY_DEALER
    }, allEntries = true)
    public Inventory save(Inventory inv) {
        return inventoryRepo.save(inv);
    }

    @Override
    @CacheEvict(value = {
            CacheConfig.CacheNames.INVENTORY_LIST,
            CacheConfig.CacheNames.INVENTORY_ONE,
            CacheConfig.CacheNames.INVENTORY_BY_BRANCH,
            CacheConfig.CacheNames.INVENTORY_BY_DEALER
    }, allEntries = true)
    public void delete(Long id) {
        inventoryRepo.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {
            CacheConfig.CacheNames.INVENTORY_LIST,
            CacheConfig.CacheNames.INVENTORY_BY_BRANCH,
            CacheConfig.CacheNames.INVENTORY_BY_DEALER
    }, allEntries = true)
    public Inventory createInventory(Inventory inv, String createdBy) {

        Inventory saved = inventoryRepo.save(inv);

        Integer onHand = Optional.ofNullable(saved.getQtyOnHand()).orElse(0);
        if (onHand > 0) {
            LocalDateTime now = LocalDateTime.now();
            InventoryAdjustment adj = new InventoryAdjustment();
            adj.setInventory(saved);
            adj.setDeltaQty(onHand);
            adj.setReason("Initial stock");
            adj.setCreatedAtEvent(now);
            adj.setCreatedAt(now);
            adj.setUpdatedAt(now);
            adj.setCreatedBy(createdBy);
            adj.setUpdatedBy(createdBy);

            inventoryAdjustmentRepo.save(adj);

            // ⬅️ NiFi notify
            nifiService.sendToNifi(saved);
        }

        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = {
            CacheConfig.CacheNames.INVENTORY_LIST,
            CacheConfig.CacheNames.INVENTORY_BY_DEALER,
            CacheConfig.CacheNames.INVENTORY_BY_BRANCH
    }, allEntries = true)
    public Inventory updateInventory(InventoryUpdateRequest req, String updatedBy) {

        Inventory current = inventoryRepo.findById(req.getId())
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + req.getId()));

        Integer oldQty = Optional.ofNullable(current.getQtyOnHand()).orElse(0);
        Integer newQty = Optional.ofNullable(req.getQtyOnHand()).orElse(0);

        current.setQtyOnHand(newQty);

        Inventory saved = inventoryRepo.save(current);

        int delta = newQty - oldQty;
        if (delta != 0) {
            LocalDateTime now = LocalDateTime.now();
            InventoryAdjustment adj = new InventoryAdjustment();
            adj.setInventory(saved);
            adj.setDeltaQty(delta);
            adj.setReason(req.getNote());
            adj.setCreatedAtEvent(now);
            adj.setCreatedAt(now);
            adj.setUpdatedAt(now);
            adj.setCreatedBy(updatedBy);
            adj.setUpdatedBy(updatedBy);

            inventoryAdjustmentRepo.save(adj);
        }

        // ⬅️ NiFi sync
        nifiService.sendToNifi(saved);

        return saved;
    }

    @Override
    @Cacheable(value = CacheConfig.CacheNames.INVENTORY_ADJUSTMENTS, key = "#inventoryId")
    public List<InventoryAdjustment> getAdjustmentsForInventory(Long inventoryId) {
        return inventoryAdjustmentRepo.findByInventoryIdOrderByCreatedAtEventDesc(inventoryId);
    }

    @Override
    @Cacheable(value = CacheConfig.CacheNames.INVENTORY_BY_DEALER, key = "#dealerId")
    public Map<Long, Integer> getStockByTrimForDealer(Long dealerId) {

        var invList = inventoryRepo.findByDealer_Id(dealerId);
        Map<Long, Integer> stockMap = new HashMap<>();

        for (var inv : invList) {
            if (inv.getTrim() == null) continue;

            Long trimId = inv.getTrim().getId();
            Integer qty = Optional.ofNullable(inv.getQtyOnHand()).orElse(0);

            stockMap.merge(trimId, qty, Integer::sum);
        }

        return stockMap;
    }

    @Override
    @Cacheable(value = CacheConfig.CacheNames.INVENTORY_BY_BRANCH, key = "#branchId")
    public Map<Long, Integer> getStockByTrimForBranch(Long branchId) {

        var rows = inventoryRepo.sumAvailableByTrimAtBranch(branchId);
        Map<Long, Integer> map = new HashMap<>();

        for (Object[] r : rows) {
            Long trimId = ((Number) r[0]).longValue();
            Integer qty = ((Number) r[1]).intValue();
            map.put(trimId, qty);
        }
        return map;
    }

    public int getAvailableForTrimAtCurrentBranch(Long trimId, Long branchId) {
        return inventoryRepo.findByTrim_IdAndBranch_Id(trimId, branchId)
                .map(i -> Optional.ofNullable(i.getQtyOnHand()).orElse(0)
                        - Optional.ofNullable(i.getReserved()).orElse(0))
                .orElse(0);
    }

    // ============================================================
    // ========================= HELPERS ===========================
    // ============================================================

    private Long resolveTrimId(OrderItem item) {
        if (item.getTrimId() != null) return item.getTrimId();
        if (item.getVehicleId() != null) return item.getVehicleId();
        throw new IllegalStateException("No trim or vehicle id on order item " + item.getId());
    }
}

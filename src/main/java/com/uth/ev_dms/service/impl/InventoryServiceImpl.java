package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.domain.DealerBranch;
import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.InventoryAdjustment;
import com.uth.ev_dms.domain.InventoryMove;
import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.repo.DealerBranchRepo;
import com.uth.ev_dms.repo.InventoryAdjustmentRepo;
import com.uth.ev_dms.repo.InventoryMoveRepo;
import com.uth.ev_dms.repo.InventoryRepo;
import com.uth.ev_dms.repo.OrderItemRepo;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.service.InventoryService;
import com.uth.ev_dms.service.dto.InventoryUpdateRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Primary
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    // ===== Repos dùng cho ORDER FLOW =====
    private final InventoryRepo inventoryRepo;
    private final InventoryMoveRepo moveRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final DealerBranchRepo dealerBranchRepo;

    // ===== Repos dùng cho ADMIN INVENTORY =====
    private final InventoryAdjustmentRepo inventoryAdjustmentRepo;

    @PersistenceContext
    private EntityManager em;

    // ===============================
    // ========== ORDER FLOW =========
    // ===============================

    /** Allocate cho 1 item (giữ lại cho tương thích cũ) */
    @Override
    @Transactional
    public boolean allocateForOrder(OrderItem item) {
        OrderHdr order = item.getOrder();
        if (order == null || order.getDealerId() == null) {
            throw new IllegalStateException("Order or dealerId not found for item " + item.getId());
        }

        final Long dealerId = order.getDealerId();
        final Long trimId   = resolveTrimId(item);
        final int qty       = item.getQty() != null ? item.getQty() : 0;
        if (qty <= 0) return true;

        // Lấy MAIN branch theo dealer
        final Long branchId = dealerBranchRepo.findByDealerId(dealerId)
                .orElseThrow(() -> new IllegalStateException("Dealer has no MAIN branch"))
                .getId();

        // Khóa hàng tồn kho theo branch + trim (chuẩn mới)
        Inventory inv = inventoryRepo.lockByBranchAndTrim(branchId, trimId)
                .orElseGet(() -> inventoryRepo.save(
                        Inventory.builder()
                                .dealer(em.getReference(Dealer.class, dealerId))
                                .branch(em.getReference(DealerBranch.class, branchId))
                                .trim(em.getReference(Trim.class, trimId))
                                .quantity(0)
                                .reserved(0)
                                .build()
                ));

        int avail = (inv.getQuantity() == null ? 0 : inv.getQuantity())
                - (inv.getReserved() == null ? 0 : inv.getReserved());
        if (avail < qty) return false;

        inv.setReserved((inv.getReserved() == null ? 0 : inv.getReserved()) + qty);
        inventoryRepo.save(inv);

        // Log di chuyển kho (RESERVE)
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

    /** Allocate nguyên đơn (all-or-nothing) */
    @Override
    @Transactional
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

        // 1) Pre-check: đảm bảo đủ hàng cho tất cả item
        for (OrderItem it : items) {
            Long trimId = resolveTrimId(it);
            int need = it.getQty() != null ? it.getQty() : 0;

            Inventory inv = inventoryRepo.lockByBranchAndTrim(branchId, trimId)
                    .orElseGet(() -> inventoryRepo.save(
                            Inventory.builder()
                                    .dealer(em.getReference(Dealer.class, dealerId))
                                    .branch(em.getReference(DealerBranch.class, branchId))
                                    .trim(em.getReference(Trim.class, trimId))
                                    .quantity(0)
                                    .reserved(0)
                                    .build()
                    ));

            int avail = (inv.getQuantity() == null ? 0 : inv.getQuantity())
                    - (inv.getReserved() == null ? 0 : inv.getReserved());
            if (avail < need) {
                throw new IllegalStateException("Out of stock for trim=" + trimId);
            }
        }

        // 2) Reserve all
        for (OrderItem it : items) {
            if (!allocateForOrder(it)) {
                throw new IllegalStateException("Allocation failed for item " + it.getId());
            }
        }
    }

    /** Giao hàng: reserved--, quantity-- */
    @Override
    @Transactional
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
            int qty = it.getQty() != null ? it.getQty() : 0;
            if (qty <= 0) continue;

            Inventory inv = inventoryRepo.lockByBranchAndTrim(branchId, trimId)
                    .orElseThrow(() -> new IllegalStateException("Inventory not found (branch="
                            + branchId + ", trim=" + trimId + ")"));

            int reserved = inv.getReserved() == null ? 0 : inv.getReserved();
            int onHand  = inv.getQuantity() == null ? 0 : inv.getQuantity();

            if (reserved < qty || onHand < qty) {
                throw new IllegalStateException("Invalid inventory to ship for trim=" + trimId);
            }

            inv.setReserved(reserved - qty);
            inv.setQuantity(onHand - qty);
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

    /** Hủy đơn / deallocate: giải phóng reserved */
    @Override
    @Transactional
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
            int qty = it.getQty() != null ? it.getQty() : 0;
            if (qty <= 0) continue;

            Inventory inv = inventoryRepo.lockByBranchAndTrim(branchId, trimId)
                    .orElseGet(() -> inventoryRepo.save(
                            Inventory.builder()
                                    .dealer(em.getReference(Dealer.class, dealerId))
                                    .branch(em.getReference(DealerBranch.class, branchId))
                                    .trim(em.getReference(Trim.class, trimId))
                                    .quantity(0)
                                    .reserved(0)
                                    .build()
                    ));

            int reserved = inv.getReserved() == null ? 0 : inv.getReserved();
            inv.setReserved(Math.max(0, reserved - qty));
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

    // ===============================
    // ========== ADMIN FLOW =========
    // ===============================

    @Override
    public List<Inventory> findAll() {
        return inventoryRepo.findAll();
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return inventoryRepo.findById(id);
    }

    @Override
    public Inventory save(Inventory inv) {
        return inventoryRepo.save(inv);
    }

    @Override
    public void delete(Long id) {
        inventoryRepo.deleteById(id);
    }

    @Override
    @Transactional
    public Inventory createInventory(Inventory inv, String createdBy) {
        if (inv.getDealer() == null || inv.getDealer().getId() == null) {
            throw new IllegalStateException("Dealer is required");
        }
        // Tự gán MAIN branch nếu UI không truyền
        if (inv.getBranch() == null || inv.getBranch().getId() == null) {
            Long dealerId = inv.getDealer().getId();
            DealerBranch main = dealerBranchRepo.findByDealerId(dealerId)
                    .orElseThrow(() -> new IllegalStateException("Dealer has no MAIN branch"));
            inv.setBranch(main);
        }

        Inventory saved = inventoryRepo.save(inv);

        Integer onHand = saved.getQtyOnHand() == null ? 0 : saved.getQtyOnHand();
        if (onHand > 0) {
            LocalDateTime now = LocalDateTime.now();
            InventoryAdjustment adj = new InventoryAdjustment();
            adj.setInventory(saved);
            adj.setDeltaQty(onHand);             // từ 0 -> onHand
            adj.setReason("Initial stock");
            adj.setCreatedAtEvent(now);
            adj.setCreatedAt(now);
            adj.setUpdatedAt(now);
            adj.setCreatedBy(createdBy);
            adj.setUpdatedBy(createdBy);
            inventoryAdjustmentRepo.save(adj);
        }
        return saved;
    }

    @Override
    @Transactional
    public Inventory updateInventory(InventoryUpdateRequest req, String updatedBy) {
        Inventory current = inventoryRepo.findById(req.getId())
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + req.getId()));

        Integer oldQty = current.getQtyOnHand() == null ? 0 : current.getQtyOnHand();
        Integer newQty = req.getQtyOnHand() == null ? 0 : req.getQtyOnHand();

        current.setQtyOnHand(newQty);
        current.setQuantity(newQty); // đồng bộ cột quantity

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
        return saved;
    }

    @Override
    public List<InventoryAdjustment> getAdjustmentsForInventory(Long inventoryId) {
        return inventoryAdjustmentRepo.findByInventoryIdOrderByCreatedAtEventDesc(inventoryId);
    }

    @Override
    public Map<Long, Integer> getStockByTrimForDealer(Long dealerId) {
        var invList = inventoryRepo.findByDealer_Id(dealerId);
        Map<Long, Integer> stockMap = new HashMap<>();
        for (var inv : invList) {
            if (inv.getTrim() == null) continue;
            Long trimId = inv.getTrim().getId();
            Integer qty = inv.getQtyOnHand() == null ? 0 : inv.getQtyOnHand();
            stockMap.merge(trimId, qty, Integer::sum);
        }
        return stockMap;
    }

    // ===== Helper =====
    private Long resolveTrimId(OrderItem item) {
        if (item.getTrimId() != null) return item.getTrimId();
        if (item.getVehicleId() != null) return item.getVehicleId(); // fallback legacy
        throw new IllegalStateException("No trim or vehicle id on order item " + item.getId());
    }
}

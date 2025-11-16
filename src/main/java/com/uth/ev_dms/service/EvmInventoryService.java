package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.domain.DealerBranch;
import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.InventoryAdjustment;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.repo.DealerBranchRepo;
import com.uth.ev_dms.repo.DealerRepo;
import com.uth.ev_dms.repo.InventoryAdjustmentRepo;
import com.uth.ev_dms.repo.InventoryRepo;
import com.uth.ev_dms.service.dto.AdjustInventoryForm;
import com.uth.ev_dms.service.dto.TransferToDealerForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvmInventoryService {

    private final InventoryRepo inventoryRepo;
    private final InventoryAdjustmentRepo adjRepo;
    private final DealerRepo dealerRepo;
    private final DealerBranchRepo dealerBranchRepo;

    public EvmInventoryService(InventoryRepo inventoryRepo,
                               InventoryAdjustmentRepo adjRepo,
                               DealerRepo dealerRepo,
                               DealerBranchRepo dealerBranchRepo) {
        this.inventoryRepo = inventoryRepo;
        this.adjRepo = adjRepo;
        this.dealerRepo = dealerRepo;
        this.dealerBranchRepo = dealerBranchRepo;
    }

    /* ========== LIST / COMMON ========== */

    /** Danh sách tồn kho EVM (HQ – branch == null) dùng cho evm/inventory/list */
    public List<Inventory> listEvmInventory() {
        return inventoryRepo.findByBranchIsNull();
    }

    /** Lấy một inventory theo id (dùng chung cho adjust / transfer / history) */
    public Inventory getInventory(Long id) {
        return inventoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + id));
    }

    /* ========== ADJUST INVENTORY (adjust.html) ========== */

    /** Build form adjust cho UI */
    public AdjustInventoryForm loadAdjustForm(Long inventoryId) {
        Inventory inv = getInventory(inventoryId);

        AdjustInventoryForm form = new AdjustInventoryForm();
        form.setInventoryId(inv.getId());
        form.setVehicleName(inv.getTrim().getVehicle().getModelName());
        form.setTrimName(inv.getTrim().getTrimName());

        int current = inv.getQtyOnHand() == null ? 0 : inv.getQtyOnHand();
        form.setCurrentQty(current);

        return form;
    }

    /** Thực hiện điều chỉnh ± deltaQty, lưu InventoryAdjustment */
    @Transactional
    public void adjustInventory(AdjustInventoryForm form) {
        Inventory inv = getInventory(form.getInventoryId());

        int oldQty = inv.getQtyOnHand() == null ? 0 : inv.getQtyOnHand();
        int delta  = form.getDeltaQty() == null ? 0 : form.getDeltaQty();
        int newQty = oldQty + delta;
        if (newQty < 0) {
            throw new IllegalArgumentException("Số lượng sau điều chỉnh không được âm");
        }

        inv.setQtyOnHand(newQty);
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryRepo.save(inv);

        if (delta != 0) {
            LocalDateTime now = LocalDateTime.now();

            InventoryAdjustment adj = new InventoryAdjustment();
            adj.setInventory(inv);
            adj.setDeltaQty(delta);
            adj.setReason(form.getReason());
            adj.setCreatedAtEvent(now);

            adj.setCreatedAt(now);      // ⭐ bắt buộc nếu DB NOT NULL
            adj.setUpdatedAt(now);
            adj.setCreatedBy("EVM Staff");
            adj.setUpdatedBy("EVM Staff");

            adjRepo.save(adj);
        }

    }

    /** Lịch sử điều chỉnh của 1 inventory */
    public List<InventoryAdjustment> listAdjustments(Long inventoryId) {
        return adjRepo.findByInventoryIdOrderByCreatedAtEventDesc(inventoryId);
    }

    /* ========== TRANSFER TO DEALER (transfer.html) ========== */

    /** Load dữ liệu cho form transfer */
    public TransferToDealerForm loadTransferForm(Long inventoryId) {
        Inventory inv = getInventory(inventoryId);

        TransferToDealerForm form = new TransferToDealerForm();
        form.setInventoryId(inv.getId());
        form.setVehicleName(inv.getTrim().getVehicle().getModelName());
        form.setTrimName(inv.getTrim().getTrimName());

        int onHand   = inv.getQtyOnHand() == null ? 0 : inv.getQtyOnHand();
        int reserved = inv.getReserved() == null ? 0 : inv.getReserved();
        int available = onHand - reserved;
        form.setAvailableQty(available);

        return form;
    }

    /** Danh sách dealer để đổ vào dropdown */
    public List<Dealer> listDealers() {
        return dealerRepo.findAll();
    }

    /** Thực hiện chuyển từ HQ (EVM) sang MAIN branch của dealer */
    @Transactional
    public void transferToDealer(Long evmInventoryId, TransferToDealerForm form) {
        Inventory evmInv = getInventory(evmInventoryId);

        // chỉ cho phép transfer từ HQ (branch null)
        if (evmInv.getBranch() != null) {
            throw new IllegalStateException("Inventory " + evmInventoryId + " không phải kho HQ");
        }

        int qty = form.getQuantity() == null ? 0 : form.getQuantity();
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity phải > 0");
        }

        int onHand   = evmInv.getQtyOnHand() == null ? 0 : evmInv.getQtyOnHand();
        int reserved = evmInv.getReserved() == null ? 0 : evmInv.getReserved();
        int available = onHand - reserved;
        if (qty > available) {
            throw new IllegalArgumentException("Không đủ tồn kho EVM để chuyển (available = " + available + ")");
        }

        Dealer dealer = dealerRepo.findById(form.getDealerId())
                .orElseThrow(() -> new IllegalArgumentException("Dealer không tồn tại"));

        // tìm MAIN branch của dealer
        DealerBranch mainBranch = dealerBranchRepo.findByDealerId(dealer.getId())
                .orElseThrow(() -> new IllegalStateException("Dealer không có MAIN branch"));

        Trim trim = evmInv.getTrim();

        // 🔧 SỬA CHỖ NÀY:
        // Thay vì tìm theo (trim, branch), ta lock theo (dealer, trim)
        // đúng với unique key (dealer_id, trim_id) và mô hình 1 dealer 1 branch.
        Inventory dealerInv = inventoryRepo.lockByDealerAndTrim(dealer.getId(), trim.getId())
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setDealer(dealer);
                    inv.setTrim(trim);
                    inv.setQtyOnHand(0);
                    inv.setReserved(0);
                    return inv;
                });

        // đảm bảo branch luôn là MAIN branch (migrate record cũ nếu cần)
        dealerInv.setBranch(mainBranch);

        // cập nhật HQ
        evmInv.setQtyOnHand(onHand - qty);
        evmInv.setUpdatedAt(LocalDateTime.now());
        inventoryRepo.save(evmInv);

        // adjustment OUT cho EVM
        LocalDateTime now = LocalDateTime.now();

        InventoryAdjustment out = new InventoryAdjustment();
        out.setInventory(evmInv);
        out.setDeltaQty(-qty);
        out.setReason("TRANSFER_OUT to dealer " + dealer.getName()
                + (form.getNote() != null ? " (" + form.getNote() + ")" : ""));
        out.setCreatedAtEvent(now);

        out.setCreatedAt(now);      // ⭐
        out.setUpdatedAt(now);
        out.setCreatedBy("EVM Staff");
        out.setUpdatedBy("EVM Staff");

        adjRepo.save(out);

        // cập nhật dealer branch (inventory)
        int dealerOnHand = dealerInv.getQtyOnHand() == null ? 0 : dealerInv.getQtyOnHand();
        dealerInv.setQtyOnHand(dealerOnHand + qty);
        dealerInv.setUpdatedAt(LocalDateTime.now());
        inventoryRepo.save(dealerInv);

        // adjustment IN cho dealer
        InventoryAdjustment in = new InventoryAdjustment();
        in.setInventory(dealerInv);
        in.setDeltaQty(qty);
        in.setReason("TRANSFER_IN from EVM"
                + (form.getNote() != null ? " (" + form.getNote() + ")" : ""));
        in.setCreatedAtEvent(now);

        in.setCreatedAt(now);       // ⭐
        in.setUpdatedAt(now);
        in.setCreatedBy("EVM Staff");
        in.setUpdatedBy("EVM Staff");

        adjRepo.save(in);

    }
}

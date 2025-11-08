package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.service.dto.AdjustInventoryForm;
import com.uth.ev_dms.service.dto.TransferToDealerForm;
import com.uth.ev_dms.repo.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvmInventoryService {

    private final InventoryRepo inventoryRepo;
    private final InventoryAdjustmentRepo adjRepo;
    private final DealerRepo dealerRepo;   // nếu chưa có, tạo JpaRepository<Dealer, Long>

    public EvmInventoryService(InventoryRepo inventoryRepo,
                               InventoryAdjustmentRepo adjRepo,
                               DealerRepo dealerRepo) {
        this.inventoryRepo = inventoryRepo;
        this.adjRepo = adjRepo;
        this.dealerRepo = dealerRepo;
    }

    // ========== LIST ==========
    public List<Inventory> listEvmInventories() {
        return inventoryRepo.findByLocationTypeOrderByUpdatedAtDesc("EVM");
    }

    // ========== GET ==========
    public Inventory getInventory(Long id) {
        return inventoryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + id));
    }

    // ========== HISTORY ==========
    public List<InventoryAdjustment> listAdjustments(Long inventoryId) {
        return adjRepo.findByInventoryIdOrderByCreatedAtEventDesc(inventoryId);
    }

    // ========== ADJUST ==========
    public AdjustInventoryForm buildAdjustForm(Long inventoryId) {
        Inventory inv = getInventory(inventoryId);

        AdjustInventoryForm f = new AdjustInventoryForm();
        f.setInventoryId(inv.getId());
        f.setVehicleName(inv.getTrim().getVehicle().getModelName());
        f.setTrimName(inv.getTrim().getTrimName());
        f.setCurrentQty(inv.getQtyOnHand());
        return f;
    }

    @Transactional
    public void adjustEvmInventory(Long inventoryId, AdjustInventoryForm form) {
        Inventory inv = getInventory(inventoryId);
        int after = (inv.getQtyOnHand() == null ? 0 : inv.getQtyOnHand())
                + (form.getDeltaQty() == null ? 0 : form.getDeltaQty());

        if (after < 0) throw new IllegalArgumentException("Số lượng sau điều chỉnh < 0");

        inv.setQtyOnHand(after);
        inv.setUpdatedAt(LocalDateTime.from(Instant.now()));
        // inv.setUpdatedBy(currentUser)

        inventoryRepo.save(inv);

        InventoryAdjustment log = new InventoryAdjustment();
        log.setInventory(inv);
        log.setDeltaQty(form.getDeltaQty());
        log.setReason(form.getReason());
        log.setCreatedAtEvent(LocalDateTime.from(Instant.now()));
        log.setCreatedBy("EVM Staff"); // TODO lấy user thật
        adjRepo.save(log);
    }

    // ========== TRANSFER to dealer ==========
    public TransferToDealerForm buildTransferForm(Long inventoryId) {
        Inventory inv = getInventory(inventoryId);

        TransferToDealerForm f = new TransferToDealerForm();
        f.setInventoryId(inv.getId());
        f.setVehicleName(inv.getTrim().getVehicle().getModelName());
        f.setTrimName(inv.getTrim().getTrimName());
        f.setAvailableQty(inv.getQtyOnHand());
        return f;
    }

    public List<Dealer> listDealers() {
        return dealerRepo.findAll();
    }

    @Transactional
    public void transferToDealer(Long evmInventoryId, TransferToDealerForm form) {
        Inventory evmInv = getInventory(evmInventoryId);
        int qty = form.getQuantity() == null ? 0 : form.getQuantity();
        if (qty <= 0) throw new IllegalArgumentException("Quantity phải > 0");

        int current = evmInv.getQtyOnHand() == null ? 0 : evmInv.getQtyOnHand();
        if (qty > current) throw new IllegalArgumentException("Không đủ tồn kho EVM để chuyển");

        Dealer dealer = dealerRepo.findById(form.getDealerId())
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found: " + form.getDealerId()));

        // Giảm EVM
        evmInv.setQtyOnHand(current - qty);
        evmInv.setUpdatedAt(LocalDateTime.from(Instant.now()));
        inventoryRepo.save(evmInv);

        InventoryAdjustment out = new InventoryAdjustment();
        out.setInventory(evmInv);
        out.setDeltaQty(-qty);
        out.setReason("TRANSFER_OUT -> Dealer#" + dealer.getId() + (form.getNote() != null ? " ("+form.getNote()+")" : ""));
        out.setCreatedAtEvent(LocalDateTime.from(Instant.now()));
        out.setCreatedBy("EVM Staff");
        adjRepo.save(out);

        // Tăng Dealer (upsert inventory dealer)
        Inventory dealerInv = inventoryRepo
                .findByTrimIdAndLocationTypeAndDealerId(evmInv.getTrim().getId(), "DEALER", dealer.getId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setTrim(evmInv.getTrim());
                    newInv.setLocationType("DEALER");
                    newInv.setDealer(dealer);
                    newInv.setQtyOnHand(0);
                    return newInv;
                });
        dealerInv.setQtyOnHand((dealerInv.getQtyOnHand() == null ? 0 : dealerInv.getQtyOnHand()) + qty);
        dealerInv.setUpdatedAt(LocalDateTime.from(Instant.now()));
        inventoryRepo.save(dealerInv);

        InventoryAdjustment in = new InventoryAdjustment();
        in.setInventory(dealerInv);
        in.setDeltaQty(qty);
        in.setReason("TRANSFER_IN from EVM" + (form.getNote() != null ? " ("+form.getNote()+")" : ""));
        in.setCreatedAtEvent(LocalDateTime.from(Instant.now()));
        in.setCreatedBy("EVM Staff");
        adjRepo.save(in);
    }
}


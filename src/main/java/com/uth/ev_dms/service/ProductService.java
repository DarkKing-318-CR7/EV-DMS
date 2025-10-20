package com.uth.ev_dms.service;


import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final VehicleRepo vehicleRepo;
    private final TrimRepo trimRepo;
    private final PriceListRepo priceListRepo;

    // ===== Vehicle =====
    public Vehicle saveVehicle(Vehicle v) {
        // (dự phòng) nếu không dùng @PrePersist
        if (v.getWarrantyMonths() == null) v.setWarrantyMonths(0);
        return vehicleRepo.save(v);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listVehicles() { return vehicleRepo.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Vehicle> getVehicle(Long id) {
        return vehicleRepo.findById(id);
    }

    // ===== Trim =====
    @Transactional(readOnly = true)
    public List<Trim> listTrimsByVehicle(Long vehicleId) {
        return trimRepo.findByVehicleId(vehicleId);
    }

    public Trim saveTrim(Trim trim) {  // create/update
        return trimRepo.save(trim);
    }
    public Optional<Trim> getTrim(Long id){ return trimRepo.findById(id); }
    @Transactional(readOnly = true)
    public boolean hasPricesForTrim(Long trimId) {
        return !priceListRepo.findByTrimIdOrderByEffectiveFromDesc(trimId).isEmpty();
    }

    public void deleteTrim(Long trimId) {
        // Nếu muốn chặn xoá khi còn price, giữ check này:
        if (hasPricesForTrim(trimId)) {
            throw new IllegalStateException("Cannot delete trim that has price lists.");
        }
        trimRepo.deleteById(trimId);
    }


    @Transactional(readOnly = true)
    public Vehicle getVehicleOrThrow(Long id) {
        return vehicleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
    }


    // ===== Price (versioning theo hiệu lực) =====
    @Transactional(readOnly = true)
    public List<PriceList> listPricesByTrim(Long trimId) {
        return priceListRepo.findByTrimIdOrderByEffectiveFromDesc(trimId);
    }

    @Transactional(readOnly = true)
    public PriceList currentPriceForTrim(Long trimId) {
        return priceListRepo
                .findTopActiveByTrimAtDate(trimId, LocalDate.now())
                .orElse(null);
    }

    public PriceList savePrice(PriceList price) {
        // business nhỏ: normalize range
        if (price.getEffectiveTo() != null &&
                price.getEffectiveFrom() != null &&
                price.getEffectiveTo().isBefore(price.getEffectiveFrom())) {
            price.setEffectiveTo(null);
        }
        return priceListRepo.save(price);
    }

    public void deactivatePrice(Long priceId) {
        priceListRepo.findById(priceId).ifPresent(p -> p.setActive(false));
        // @PreUpdate trong entity sẽ tự set updatedAt khi flush
    }

    @Transactional(readOnly = true)
    public Trim getTrimOrThrow(Long id) {
        return trimRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trim not found: " + id));
    }



}

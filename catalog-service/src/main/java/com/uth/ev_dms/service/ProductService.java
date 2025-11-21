package com.uth.ev_dms.service;

import com.uth.ev_dms.config.CacheConfig;
import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.dto.TrimCommercialForm;
import com.uth.ev_dms.service.dto.TrimPricingForm;
import com.uth.ev_dms.service.dto.VehicleCommercialForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final VehicleRepo vehicleRepo;
    private final TrimRepo trimRepo;
    private final PriceListRepo priceListRepo;

    // ===========================================
    // =============== VEHICLE ===================
    // ===========================================

    @CacheEvict(
            value = { CacheConfig.CacheNames.VEHICLES },
            allEntries = true
    )
    @Transactional
    public Vehicle saveVehicle(Vehicle v){
        if (v.getModelCode() == null || v.getModelCode().isBlank())
            throw new IllegalArgumentException("Model code is required");

        if (vehicleRepo.existsByModelCodeAndIdNot(
                v.getModelCode(),
                v.getId() == null ? -1L : v.getId()
        )) {
            throw new IllegalStateException("Model code already exists");
        }

        if (v.getWarrantyMonths() == null) v.setWarrantyMonths(0);

        return vehicleRepo.save(v);
    }

    @Cacheable(value = CacheConfig.CacheNames.VEHICLES)
    @Transactional(readOnly = true)
    public List<Vehicle> listVehicles() {
        return vehicleRepo.findAll();
    }

    @Cacheable(
            value = CacheConfig.CacheNames.VEHICLES,
            key = "#id"
    )
    @Transactional(readOnly = true)
    public Optional<Vehicle> getVehicle(Long id) {
        return vehicleRepo.findById(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CacheNames.VEHICLES,
            key = "'withTrims'"
    )
    public List<Vehicle> getVehiclesWithTrims() {
        return vehicleRepo.findAll();
    }

    @CacheEvict(
            value = { CacheConfig.CacheNames.VEHICLES },
            allEntries = true
    )
    public void deleteVehicle(Long id) {
        vehicleRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CacheNames.VEHICLES,
            key = "'vehicle_' + #id"
    )
    public Vehicle getVehicleOrThrow(Long id) {
        return vehicleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + id
                ));
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepo.findById(id).orElse(null);
    }


    // ===========================================
    // ================= TRIM ====================
    // ===========================================

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CacheNames.TRIMS,
            key = "'byVehicle_' + #vehicleId"
    )
    public List<Trim> listTrimsByVehicle(Long vehicleId) {
        return trimRepo.findByVehicleId(vehicleId);
    }

    @Cacheable(
            value = CacheConfig.CacheNames.TRIMS,
            key = "#id"
    )
    public Optional<Trim> getTrim(Long id){
        return trimRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean hasPricesForTrim(Long trimId) {
        return !priceListRepo.findByTrimIdOrderByEffectiveFromDesc(trimId).isEmpty();
    }

    @CacheEvict(
            value = CacheConfig.CacheNames.TRIMS,
            allEntries = true
    )
    public Trim saveTrim(Trim trim) {
        return trimRepo.save(trim);
    }

    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.TRIMS,
                    CacheConfig.CacheNames.PRICE_LISTS
            },
            allEntries = true
    )
    public void deleteTrim(Long trimId) {
        if (hasPricesForTrim(trimId))
            throw new IllegalStateException("Cannot delete trim that has price lists.");
        trimRepo.deleteById(trimId);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CacheNames.TRIMS,
            key = "'trimOrThrow_' + #id"
    )
    public Trim getTrimOrThrow(Long id) {
        return trimRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trim not found: " + id));
    }

    public List<Trim> listAllTrims() {
        return trimRepo.findAll();
    }

    public List<Trim> getAllTrims() {
        return trimRepo.findAll();
    }


    // ===========================================
    // ================= PRICE ===================
    // ===========================================

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CacheNames.PRICE_LISTS,
            key = "'byTrim_' + #trimId"
    )
    public List<PriceList> listPricesByTrim(Long trimId) {
        return priceListRepo.findByTrimIdOrderByEffectiveFromDesc(trimId);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CacheNames.PRICE_LISTS,
            key = "'current_' + #trimId"
    )
    public PriceList currentPriceForTrim(Long trimId) {
        return priceListRepo.findTopActiveByTrimAtDate(trimId, LocalDate.now())
                .orElse(null);
    }

    @CacheEvict(
            value = CacheConfig.CacheNames.PRICE_LISTS,
            allEntries = true
    )
    public PriceList savePrice(PriceList price) {
        if (price.getEffectiveTo() != null &&
                price.getEffectiveFrom() != null &&
                price.getEffectiveTo().isBefore(price.getEffectiveFrom())) {
            price.setEffectiveTo(null);
        }
        return priceListRepo.save(price);
    }

    @CacheEvict(
            value = CacheConfig.CacheNames.PRICE_LISTS,
            allEntries = true
    )
    public void deactivatePrice(Long priceId) {
        priceListRepo.findById(priceId).ifPresent(p -> p.setActive(false));
    }


    // ===========================================
    // ====== VEHICLE COMMERCIAL (EVM UI) ========
    // ===========================================

    @Transactional(readOnly = true)
    public VehicleCommercialForm getVehicleCommercialForm(Long vehicleId) {

        Vehicle v = vehicleRepo.findById(vehicleId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Vehicle not found: " + vehicleId));

        VehicleCommercialForm form = new VehicleCommercialForm();
        form.setId(v.getId());

        form.setModelCode(v.getModelCode());
        form.setModelName(v.getModelName());
        form.setBodyType(v.getBodyType());
        form.setWarrantyMonths(v.getWarrantyMonths());

        form.setRegionalStatus(
                v.getRegionalStatus() != null ? v.getRegionalStatus() : "AVAILABLE"
        );
        form.setSalesNote(
                v.getSalesNote() != null ? v.getSalesNote() : ""
        );
        form.setMarketingDesc(
                v.getMarketingDesc() != null ? v.getMarketingDesc() : ""
        );

        return form;
    }

    @CacheEvict(
            value = CacheConfig.CacheNames.VEHICLES,
            allEntries = true
    )
    @Transactional
    public void updateVehicleCommercialInfo(Long vehicleId, VehicleCommercialForm form) {

        Vehicle v = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Vehicle not found: " + vehicleId
                ));

        v.setRegionalStatus(form.getRegionalStatus());
        v.setSalesNote(form.getSalesNote());
        v.setMarketingDesc(form.getMarketingDesc());

        v.setUpdatedAt(java.time.Instant.now());

        vehicleRepo.save(v);
    }


    // ===========================================
    // ========= TRIM COMMERCIAL (EVM UI) ========
    // ===========================================

    @Transactional(readOnly = true)
    public TrimCommercialForm getTrimCommercialForm(Long trimId) {
        Trim t = trimRepo.findById(trimId)
                .orElseThrow(() -> new EntityNotFoundException("Trim not found: " + trimId));

        TrimCommercialForm form = new TrimCommercialForm();
        form.setId(t.getId());

        form.setTrimName(t.getTrimName());
        form.setDrive(String.valueOf(t.getDrive()));
        form.setBatterykwh(t.getBatteryKWh());
        form.setPowerHp(t.getPowerHp());
        form.setRangeKm(t.getRangeKm());

        form.setRegionalName(
                t.getRegionalName() != null ? t.getRegionalName() : t.getTrimName()
        );
        form.setAvailabilityNote(
                t.getAvailabilityNote() != null ? t.getAvailabilityNote() : ""
        );
        form.setAvailable(
                t.getAvailable() != null ? t.getAvailable() : Boolean.TRUE
        );

        return form;
    }

    @CacheEvict(
            value = CacheConfig.CacheNames.TRIMS,
            allEntries = true
    )
    @Transactional
    public void updateTrimCommercialInfo(Long trimId, TrimCommercialForm form) {

        Trim t = trimRepo.findById(trimId)
                .orElseThrow(() -> new EntityNotFoundException("Trim not found: " + trimId));

        t.setRegionalName(form.getRegionalName());
        t.setAvailabilityNote(form.getAvailabilityNote());
        t.setAvailable(form.getAvailable());

        t.setUpdatedAt(java.time.Instant.now());

        trimRepo.save(t);
    }


    // ===========================================
    // ============ TRIM PRICING FORM ============
    // ===========================================

    @Transactional(readOnly = true)
    public TrimPricingForm getTrimPricingForm(Long trimId) {

        Trim t = trimRepo.findById(trimId)
                .orElseThrow(() -> new EntityNotFoundException("Trim not found: " + trimId));

        List<PriceList> prices = priceListRepo.findByTrimId(trimId);

        PriceList pl = prices.isEmpty() ? null : prices.get(prices.size() - 1);

        TrimPricingForm form = new TrimPricingForm();
        form.setTrimId(t.getId());
        form.setTrimName(
                t.getRegionalName() != null && !t.getRegionalName().isBlank()
                        ? t.getRegionalName()
                        : t.getTrimName()
        );

        if (pl != null) {
            form.setBasePriceVnd(pl.getBasePriceVnd());
            form.setCurrency(pl.getCurrency());
        } else {
            form.setBasePriceVnd(0);
            form.setCurrency("VND");
        }

        return form;
    }

    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.PRICE_LISTS,
                    CacheConfig.CacheNames.TRIMS
            },
            allEntries = true
    )
    @Transactional
    public void updateTrimPricing(Long trimId, TrimPricingForm form) {

        Trim t = trimRepo.findById(trimId)
                .orElseThrow(() -> new EntityNotFoundException("Trim not found: " + trimId));

        List<PriceList> prices = priceListRepo.findByTrimId(trimId);

        PriceList pl;
        if (prices.isEmpty()) {
            pl = new PriceList();
            pl.setTrim(t);
        } else {
            pl = prices.get(prices.size() - 1);
        }

        pl.setBasePriceVnd(form.getBasePriceVnd());
        pl.setCurrency(form.getCurrency() != null ? form.getCurrency() : "VND");

        pl.setUpdatedAt(java.time.Instant.now());

        priceListRepo.save(pl);
    }

}

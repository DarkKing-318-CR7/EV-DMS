package com.uth.ev_dms.service;


import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import com.uth.ev_dms.service.dto.TrimCommercialForm;
import com.uth.ev_dms.service.dto.TrimPricingForm;
import com.uth.ev_dms.service.dto.VehicleCommercialForm;
import jakarta.persistence.EntityNotFoundException;
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
    @Transactional
    public Vehicle saveVehicle(Vehicle v){
        // ví dụ: unique modelCode
        if (v.getModelCode() == null || v.getModelCode().isBlank())
            throw new IllegalArgumentException("Model code is required");
        if (vehicleRepo.existsByModelCodeAndIdNot(v.getModelCode(), v.getId() == null ? -1L : v.getId()))
            throw new IllegalStateException("Model code already exists");
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


    public void deleteVehicle(Long id) {
        vehicleRepo.deleteById(id);
    }

    public List<Trim> listAllTrims() {
        return trimRepo.findAll();
    }

    public List<Trim> getAllTrims() {
        return trimRepo.findAll();
    }

    public List<Vehicle> getVehiclesWithTrims() {
        return vehicleRepo.findAll();
        // hoặc vehicleRepository.findAll(); (miễn sao lazy không bể view)
    }

    @Transactional(readOnly = true)
    public VehicleCommercialForm getVehicleCommercialForm(Long vehicleId) {
        Vehicle v = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + vehicleId));

        VehicleCommercialForm form = new VehicleCommercialForm();
        form.setId(v.getId());

        // readonly info
        form.setModelCode(v.getModelCode());
        form.setModelName(v.getModelName());
        form.setBodyType(v.getBodyType());
        form.setWarrantyMonths(v.getWarrantyMonths());

        // editable info (nếu bạn đã thêm cột vào entity Vehicle)
        form.setRegionalStatus(
                // nếu chưa có field trong entity -> mock default:
                (v.getRegionalStatus() != null ? v.getRegionalStatus() : "AVAILABLE")
        );
        form.setSalesNote(
                (v.getSalesNote() != null ? v.getSalesNote() : "")
        );
        form.setMarketingDesc(
                (v.getMarketingDesc() != null ? v.getMarketingDesc() : "")
        );

        return form;
    }

    // Lưu dữ liệu sau khi submit form
    @Transactional
    public void updateVehicleCommercialInfo(Long vehicleId, VehicleCommercialForm form) {
        Vehicle v = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + vehicleId));

        // Copy từ form vào entity
        // (nếu DB và entity Vehicle đã có các field này)
        v.setRegionalStatus(form.getRegionalStatus());
        v.setSalesNote(form.getSalesNote());
        v.setMarketingDesc(form.getMarketingDesc());

        // update audit: updated_at / updated_by nếu bạn có
        v.setUpdatedAt(java.time.Instant.now());
        // v.setUpdatedBy(currentUserName); // nếu track user

        vehicleRepo.save(v);
    }

    @Transactional(readOnly = true)
    public TrimCommercialForm getTrimCommercialForm(Long trimId) {
        Trim t = trimRepo.findById(trimId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Trim not found: " + trimId
                ));

        TrimCommercialForm form = new TrimCommercialForm();
        form.setId(t.getId());

        // spec gốc (readonly)
        form.setTrimName(t.getTrimName());
        form.setDrive(String.valueOf(t.getDrive())); // nếu entity Trim của bạn có field "drive" kiểu String/enum
        form.setBatterykwh(t.getBatteryKWh()); // batterykwh trong Trim entity
        form.setPowerHp(t.getPowerHp());       // power_hp
        form.setRangeKm(t.getRangeKm());       // range_km

        // phần EVM được sửa
        // nếu DB chưa có các field này thì mock default
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

    @Transactional
    public void updateTrimCommercialInfo(Long trimId, TrimCommercialForm form) {
        Trim t = trimRepo.findById(trimId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Trim not found: " + trimId
                ));

        // copy field được EVM chỉnh
        t.setRegionalName(form.getRegionalName());
        t.setAvailabilityNote(form.getAvailabilityNote());
        t.setAvailable(form.getAvailable());

        // audit
        t.setUpdatedAt(java.time.Instant.now());
//        t.setUpdatedBy("EVM Staff"); // TODO: lấy user thật nếu có

        trimRepo.save(t);
    }

    @Transactional(readOnly = true)
    public TrimPricingForm getTrimPricingForm(Long trimId) {
        Trim t = trimRepo.findById(trimId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Trim not found: " + trimId
                ));

        // lấy tất cả price rows cho trim này
        List<PriceList> prices = priceListRepo.findByTrimId(trimId);

        // chọn 1 record để hiển thị (ví dụ: record cuối cùng trong list)
        PriceList pl = null;
        if (!prices.isEmpty()) {
            // lấy cuối list (coi như latest)
            pl = prices.get(prices.size() - 1);
        }

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


    @Transactional
    public void updateTrimPricing(Long trimId, TrimPricingForm form) {
        Trim t = trimRepo.findById(trimId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Trim not found: " + trimId
                ));

        // Lấy danh sách giá hiện có
        List<PriceList> prices = priceListRepo.findByTrimId(trimId);

        PriceList pl;
        if (prices.isEmpty()) {
            // chưa có record → tạo mới
            pl = new PriceList();
            pl.setTrim(t);
        } else {
            // đã có record → cập nhật record cuối cùng
            pl = prices.get(prices.size() - 1);
        }

        pl.setBasePriceVnd(form.getBasePriceVnd());
        pl.setCurrency(
                form.getCurrency() != null ? form.getCurrency() : "VND"
        );

        pl.setUpdatedAt(java.time.Instant.now());
//        pl.setUpdatedBy("EVM Staff");

        priceListRepo.save(pl);
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepo.findById(id)
                .orElse(null);
    }

}

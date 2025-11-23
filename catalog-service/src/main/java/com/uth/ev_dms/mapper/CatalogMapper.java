package com.uth.ev_dms.mapper;

import com.uth.ev_dms.domain.PriceList;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.dto.PriceListDto;
import com.uth.ev_dms.dto.TrimDto;
import com.uth.ev_dms.dto.VehicleDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CatalogMapper {

    public VehicleDto toVehicleDto(Vehicle v) {
        if (v == null) return null;

        VehicleDto dto = new VehicleDto();
        dto.setId(v.getId());
        dto.setModelCode(v.getModelCode());
        dto.setModelName(v.getModelName());
        dto.setBrand(v.getBrand());
        dto.setBodyType(v.getBodyType());

        dto.setRegionalStatus(v.getRegionalStatus());
        dto.setSalesNote(v.getSalesNote());
        dto.setMarketingDesc(v.getMarketingDesc());

        // Vehicle hiện không có basePriceVnd / currency -> để trống
        return dto;
    }

    public TrimDto toTrimDto(Trim t) {
        if (t == null) return null;

        TrimDto dto = new TrimDto();
        dto.setId(t.getId());

        if (t.getVehicle() != null) {
            dto.setVehicleId(t.getVehicle().getId());
            dto.setVehicleModelCode(t.getVehicle().getModelCode());
            dto.setVehicleModelName(t.getVehicle().getModelName());
        }

        dto.setTrimName(t.getTrimName());

        // drive: enum -> String
        dto.setDrive(t.getDrive() != null ? t.getDrive().name() : null);

        // batteryKWh đúng theo entity Trim
        dto.setBatterykwh(t.getBatteryKWh());
        dto.setPowerHp(t.getPowerHp());
        dto.setRangeKm(t.getRangeKm());

        dto.setRegionalName(t.getRegionalName());
        dto.setAvailabilityNote(t.getAvailabilityNote());
        dto.setAvailable(t.getAvailable());

        // giá hiện tại: dùng helper getCurrentPrice() (BigDecimal)
        BigDecimal currentPrice = t.getCurrentPrice();
        dto.setBasePriceVnd(currentPrice != null ? currentPrice.intValue() : null);
        dto.setCurrency("VND"); // hoặc sau này lấy từ PriceList active

        return dto;
    }

    public PriceListDto toPriceListDto(PriceList p) {
        if (p == null) return null;

        PriceListDto dto = new PriceListDto();
        dto.setId(p.getId());
        dto.setTrimId(p.getTrim() != null ? p.getTrim().getId() : null);
        dto.setTrimName(p.getTrim() != null ? p.getTrim().getTrimName() : null);

        dto.setBasePriceVnd(p.getBasePriceVnd());
        dto.setCurrency(p.getCurrency());
        dto.setEffectiveFrom(p.getEffectiveFrom());
        dto.setEffectiveTo(p.getEffectiveTo());

        // boolean -> isActive()
        dto.setActive(p.isActive());

        dto.setMsrp(p.getMsrp());

        return dto;
    }
}

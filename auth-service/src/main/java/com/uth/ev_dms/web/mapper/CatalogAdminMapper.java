package com.uth.ev_dms.web.mapper;

import com.uth.ev_dms.client.dto.PriceListFeignDto;
import com.uth.ev_dms.client.dto.TrimFeignDto;
import com.uth.ev_dms.client.dto.VehicleFeignDto;
import com.uth.ev_dms.web.vm.PriceListVm;
import com.uth.ev_dms.web.vm.TrimVm;
import com.uth.ev_dms.web.vm.VehicleVm;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CatalogAdminMapper {

    public VehicleVm toVehicleVm(VehicleFeignDto dto) {
        if (dto == null) return null;
        VehicleVm vm = new VehicleVm();

        vm.setId(dto.getId());
        vm.setModelCode(dto.getModelCode());
        vm.setModelName(dto.getModelName());
        vm.setBrand(dto.getBrand());
        vm.setBodyType(dto.getBodyType());
        vm.setWarrantyMonths(dto.getWarrantyMonths());
        vm.setRegionalStatus(dto.getRegionalStatus());
        vm.setSalesNote(dto.getSalesNote());
        vm.setMarketingDesc(dto.getMarketingDesc());

        return vm;
    }

    public List<VehicleVm> toVehicleVmList(List<VehicleFeignDto> list) {
        return list == null ? List.of()
                : list.stream().map(this::toVehicleVm).collect(Collectors.toList());
    }

    public TrimVm toTrimVm(TrimFeignDto dto) {
        if (dto == null) return null;
        TrimVm vm = new TrimVm();

        vm.setId(dto.getId());
        vm.setVehicleId(dto.getVehicleId());
        vm.setVehicleModelCode(dto.getVehicleModelCode());
        vm.setVehicleModelName(dto.getVehicleModelName());

        vm.setTrimName(dto.getTrimName());
        vm.setDrive(dto.getDrive());
        vm.setBatterykwh(dto.getBatterykwh());
        vm.setPowerHp(dto.getPowerHp());
        vm.setRangeKm(dto.getRangeKm());

        vm.setRegionalName(dto.getRegionalName());
        vm.setAvailabilityNote(dto.getAvailabilityNote());
        vm.setAvailable(dto.getAvailable());

        vm.setBasePriceVnd(dto.getBasePriceVnd());
        vm.setCurrency(dto.getCurrency());

        return vm;
    }

    public List<TrimVm> toTrimVmList(List<TrimFeignDto> list) {
        return list == null ? List.of()
                : list.stream().map(this::toTrimVm).collect(Collectors.toList());
    }

    public PriceListVm toPriceListVm(PriceListFeignDto dto) {
        if (dto == null) return null;
        PriceListVm vm = new PriceListVm();

        vm.setId(dto.getId());
        vm.setTrimId(dto.getTrimId());
        vm.setTrimName(dto.getTrimName());

        vm.setBasePriceVnd(dto.getBasePriceVnd());
        vm.setCurrency(dto.getCurrency());
        vm.setEffectiveFrom(dto.getEffectiveFrom());
        vm.setEffectiveTo(dto.getEffectiveTo());
        vm.setActive(dto.getActive());
        vm.setMsrp(dto.getMsrp());

        return vm;
    }

    public List<PriceListVm> toPriceListVmList(List<PriceListFeignDto> list) {
        return list == null ? List.of()
                : list.stream().map(this::toPriceListVm).collect(Collectors.toList());
    }
}

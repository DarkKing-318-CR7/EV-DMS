package com.uth.ev_dms.controllers;

import com.uth.ev_dms.client.CatalogFeignClient;
import com.uth.ev_dms.web.mapper.CatalogAdminMapper;
import com.uth.ev_dms.web.vm.PriceListVm;
import com.uth.ev_dms.web.vm.TrimVm;
import com.uth.ev_dms.web.vm.VehicleVm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/products")
public class AdminCatalogController {

    private final CatalogFeignClient catalogClient;
    private final CatalogAdminMapper mapper;

    @GetMapping()
    public String listVehicles(Model model) {
        var listDto = catalogClient.listVehicles();
        List<VehicleVm> vehicles = mapper.toVehicleVmList(listDto);
        model.addAttribute("vehicles", vehicles);
        return "admin/products/list";
    }

    @GetMapping("/vehicles/{id}")
    public String vehicleDetail(@PathVariable Long id, Model model) {
        var vDto = catalogClient.getVehicle(id);
        var vVm  = mapper.toVehicleVm(vDto);

        var trimsDto = catalogClient.listTrimsByVehicle(id);
        List<TrimVm> trims = mapper.toTrimVmList(trimsDto);

        model.addAttribute("vehicle", vVm);
        model.addAttribute("trims", trims);
        return "admin/products/detail";
    }

    @GetMapping("/trims/{trimId}/prices")
    public String trimPrices(@PathVariable Long trimId, Model model) {
        var trimDto = catalogClient.getTrim(trimId);
        TrimVm trim = mapper.toTrimVm(trimDto);

        var pricesDto = catalogClient.getPricesByTrim(trimId);
        List<PriceListVm> prices = mapper.toPriceListVmList(pricesDto);

        model.addAttribute("trim", trim);
        model.addAttribute("prices", prices);
        return "admin/prices/list";
    }
}

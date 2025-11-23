package com.uth.ev_dms.controllers;


import com.uth.ev_dms.domain.PriceList;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.domain.Vehicle;
import com.uth.ev_dms.repo.PriceListRepo;
import com.uth.ev_dms.repo.TrimRepo;
import com.uth.ev_dms.service.ProductService;
import com.uth.ev_dms.dto.PriceListDto;
import com.uth.ev_dms.dto.TrimDto;
import com.uth.ev_dms.dto.VehicleDto;
import com.uth.ev_dms.mapper.CatalogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogApiController {

    private final ProductService productService;
    private final TrimRepo trimRepo;
    private final PriceListRepo priceListRepo;
    private final CatalogMapper catalogMapper;

    // ===== VEHICLES =====
    @GetMapping("/vehicles")
    public List<VehicleDto> listVehicles() {
        List<Vehicle> vehicles = productService.listVehicles();
        return vehicles.stream().map(catalogMapper::toVehicleDto).toList();
    }

    @GetMapping("/vehicles/{id}")
    public VehicleDto getVehicle(@PathVariable Long id) {
        Vehicle v = productService.getVehicle(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
        return catalogMapper.toVehicleDto(v);
    }

    // ===== TRIMS =====
    @GetMapping("/vehicles/{vehicleId}/trims")
    public List<TrimDto> listTrimsByVehicle(@PathVariable Long vehicleId) {
        List<Trim> trims = productService.listTrimsByVehicle(vehicleId);
        return trims.stream().map(catalogMapper::toTrimDto).toList();
    }

    @GetMapping("/trims")
    public List<TrimDto> listAllTrims() {
        List<Trim> trims = productService.getAllTrims();
        return trims.stream().map(catalogMapper::toTrimDto).toList();
    }

    @GetMapping("/trims/{id}")
    public TrimDto getTrim(@PathVariable Long id) {
        Trim t = trimRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trim not found: " + id));
        return catalogMapper.toTrimDto(t);
    }

    // ===== PRICE LISTS =====
    @GetMapping("/trims/{trimId}/prices")
    public List<PriceListDto> getPricesByTrim(@PathVariable Long trimId) {
        List<PriceList> prices = priceListRepo.findByTrimIdOrderByEffectiveFromDesc(trimId);
        return prices.stream().map(catalogMapper::toPriceListDto).toList();
    }

    @GetMapping("/prices/{id}")
    public PriceListDto getPrice(@PathVariable Long id) {
        PriceList p = priceListRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Price not found: " + id));
        return catalogMapper.toPriceListDto(p);
    }
}

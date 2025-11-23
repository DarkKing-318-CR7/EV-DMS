package com.uth.ev_dms.client;

import com.uth.ev_dms.client.dto.PriceListFeignDto;
import com.uth.ev_dms.client.dto.TrimFeignDto;
import com.uth.ev_dms.client.dto.VehicleFeignDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "catalog-service",
        url = "${catalog-service.url}"  // cấu hình trong application.yml
)
public interface CatalogFeignClient {

    // VEHICLE
    @GetMapping("/api/catalog/vehicles")
    List<VehicleFeignDto> listVehicles();

    @GetMapping("/api/catalog/vehicles/{id}")
    VehicleFeignDto getVehicle(@PathVariable("id") Long id);

    // TRIM
    @GetMapping("/api/catalog/vehicles/{vehicleId}/trims")
    List<TrimFeignDto> listTrimsByVehicle(@PathVariable("vehicleId") Long vehicleId);

    @GetMapping("/api/catalog/trims/{id}")
    TrimFeignDto getTrim(@PathVariable("id") Long id);

    // PRICE LIST
    @GetMapping("/api/catalog/trims/{trimId}/prices")
    List<PriceListFeignDto> getPricesByTrim(@PathVariable("trimId") Long trimId);

    @GetMapping("/api/catalog/prices/{id}")
    PriceListFeignDto getPrice(@PathVariable("id") Long id);
}

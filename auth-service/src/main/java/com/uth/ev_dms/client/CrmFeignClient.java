package com.uth.ev_dms.client;

import com.uth.ev_dms.client.dto.CustomerFeignDto;
import com.uth.ev_dms.client.dto.TestDriveFeignDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "crm-service",
        url = "${crm_service.url}"
)
public interface CrmFeignClient {

    // ===== CUSTOMERS =====
    @GetMapping("/api/v1/crm/customers")
    List<CustomerFeignDto> listCustomers(@RequestParam(value = "q", required = false) String keyword);

    @PostMapping("/api/v1/crm/customers")
    CustomerFeignDto createCustomer(@RequestBody CustomerFeignDto dto);

    @PutMapping("/api/v1/crm/customers/{id}")
    CustomerFeignDto updateCustomer(@PathVariable("id") Long id, @RequestBody CustomerFeignDto dto);

    @DeleteMapping("/api/v1/crm/customers/{id}")
    void deleteCustomer(@PathVariable("id") Long id);

    // ===== TEST-DRIVES =====
    @GetMapping("/api/crm/test-drives")
    List<TestDriveFeignDto> listTestDrives(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "status", required = false) String status
    );
}

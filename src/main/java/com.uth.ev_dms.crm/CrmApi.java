package com.uth.ev_dms.crm;

import com.uth.ev_dms.crm.dto.CustomerCreateDto;
import com.uth.ev_dms.crm.dto.TestDriveCreateDto;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/crm")
public class CrmApi {
    private final CustomerService customerService;
    private final CrmTestDriveService testDriveService;

    public CrmApi(CustomerService customerService, CrmTestDriveService testDriveService) {
        this.customerService = customerService;
        this.testDriveService = testDriveService;
    }

    // Customers
    @GetMapping("/customers")
    public List<Customer> getCustomers() {
        return customerService.all();
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CustomerCreateDto dto) {
        return ResponseEntity.ok(customerService.create(dto));
    }

    // Test drives
    @GetMapping("/test-drives")
    public List<TestDrive> listTestDrives(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return testDriveService.list(startDate, endDate);
    }

    @PostMapping("/test-drives")
    public ResponseEntity<TestDrive> createTestDrive(@Valid @RequestBody TestDriveCreateDto dto) {
        return ResponseEntity.ok(testDriveService.create(dto));
    }

    @PatchMapping("/test-drives/{id}/status")
    public ResponseEntity<TestDrive> patchStatus(@PathVariable Long id,
                                                 @RequestParam TestDriveStatus status) {
        return ResponseEntity.ok(testDriveService.updateStatus(id, status));
    }
}

package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.dto.CustomerDto;
import com.uth.ev_dms.mapper.CrmMapper;
import com.uth.ev_dms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crm/customers")
public class CustomerApi {

    private final CustomerService customerService;
    private final CrmMapper crmMapper;

    // ================= GET LIST =================
    @GetMapping
    public List<CustomerDto> list(@RequestParam(required = false) String q) {
        List<Customer> customers;
        if (q != null && !q.isBlank()) {
            customers = customerService.searchAll(q);
        } else {
            customers = customerService.findAll();
        }
        return customers.stream()
                .map(crmMapper::toCustomerDto)
                .toList();
    }

    // ================= CREATE =================
    @PostMapping
    public CustomerDto create(@RequestBody Customer c) {
        // Không cần Authentication
        // Không cần setOwnerId
        Customer saved = customerService.create(c);
        return crmMapper.toCustomerDto(saved);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public CustomerDto update(@PathVariable Long id, @RequestBody Customer c) {
        c.setId(id);
        Customer updated = customerService.update(c);
        return crmMapper.toCustomerDto(updated);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}

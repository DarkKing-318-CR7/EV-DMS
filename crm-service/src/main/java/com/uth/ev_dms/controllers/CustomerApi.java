package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crm/customers")
public class CustomerApi {

    private final CustomerService customerService;

    // ================= GET LIST =================
    @GetMapping
    public List<Customer> list(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return customerService.searchAll(q);
        }
        return customerService.findAll();
    }

    // ================= CREATE =================
    @PostMapping
    public Customer create(@RequestBody Customer c) {
        // Không cần Authentication
        // Không cần setOwnerId
        return customerService.create(c);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody Customer c) {
        c.setId(id);
        return customerService.update(c);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}

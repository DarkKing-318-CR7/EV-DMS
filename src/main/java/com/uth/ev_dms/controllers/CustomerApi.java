package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crm")
public class CustomerApi {

    private final CustomerService customerService;

    @GetMapping("/customers")
    public List<Customer> list(Authentication auth, @RequestParam(required = false) String q) {
        Long userId = currentUserId(auth);
        boolean isManager = hasRole(auth, "ROLE_DMANAGER");
        boolean isStaff = hasRole(auth, "ROLE_DSTAFF");

        System.out.println("🟢 UserID=" + userId + ", Manager=" + isManager + ", Staff=" + isStaff);

        if (q != null && !q.isBlank()) {
            return isManager ? customerService.searchAll(q) : customerService.searchMine(userId, q);
        }

        // ✅ Cho phép Manager và Staff đều thấy danh sách mình tạo + toàn bộ nếu là Manager
        if (isManager) {
            return customerService.findAll();
        } else if (isStaff) {
            return customerService.findMine(userId);
        } else {
            return customerService.findAll(); // fallback
        }
    }
    @PostMapping("/customers")
    public Customer create(@RequestBody Customer c, Authentication auth) {
        c.setOwnerId(currentUserId(auth));
        return customerService.create(c);
    }

    @PutMapping("/customers/{id}")
    public Customer update(@PathVariable Long id, @RequestBody Customer c) {
        c.setId(id);
        return customerService.update(c);
    }

    @DeleteMapping("/customers/{id}")
    public void delete(@PathVariable Long id) { customerService.delete(id); }

    private Long currentUserId(Authentication auth) {
        try { return Long.parseLong(auth.getName()); }
        catch (Exception e) { return null; }
    }
    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}

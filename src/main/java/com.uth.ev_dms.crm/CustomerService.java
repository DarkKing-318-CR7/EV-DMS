package com.uth.ev_dms.crm;

import com.uth.ev_dms.crm.dto.CustomerCreateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepo repo;

    public CustomerService(CustomerRepo repo) { this.repo = repo; }

    public List<Customer> all() { return repo.findAll(); }

    @Transactional
    public Customer create(CustomerCreateDto dto) {
        if (repo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("email da ton tai");
        }
        Customer c = new Customer();
        c.setTen(dto.getTen());
        c.setEmail(dto.getEmail());
        c.setSdt(dto.getSdt());
        c.setDiachi(dto.getDiachi());
        return repo.save(c);
    }
}

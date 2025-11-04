package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.repo.CustomerRepo;
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo customerRepo;
    private final UserRepo userRepo;

    @Override
    public Page<Customer> myList(String username, int page, int size) {
        User u = userRepo.findByUsername(username).orElseThrow();
        return customerRepo.findByOwnerId(u.getId(), PageRequest.of(page, size));
    }

    @Override
    public Customer createForOwner(String username, String name, String email, String phone, String address) {
        User u = userRepo.findByUsername(username).orElseThrow();
        Customer c = new Customer();
        c.setTen(name);
        c.setEmail(email);
        c.setSdt(phone);
        c.setDiachi(address);
        c.setOwnerId(u.getId());
        return customerRepo.save(c);
    }

    @Override
    public Customer updateForOwner(String username, Long id, String name, String email, String phone, String address) {
        User u = userRepo.findByUsername(username).orElseThrow();
        Customer c = customerRepo.findById(id).orElseThrow();
        if (!u.getId().equals(c.getOwnerId())) {
            throw new IllegalArgumentException("Not owner of this customer");
        }
        c.setTen(name);
        c.setEmail(email);
        c.setSdt(phone);
        c.setDiachi(address);
        return customerRepo.save(c);
    }
}

package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.repo.CustomerRepo;
import com.uth.ev_dms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo repo;

    @Override
    @Transactional
    public Customer create(Customer c) {
        if (c.getSdt() != null && !c.getSdt().isBlank() && repo.existsBySdt(c.getSdt())) {
            throw new IllegalStateException("So dien thoai da ton tai");
        }

        // ✅ Nếu chưa có ownerId (Manager tạo) → để null
        // ✅ Nếu Staff tạo → ownerId sẽ được set từ API
        return repo.save(c);
    }


    @Override
    public Customer update(Customer c) {
        if (c.getId() != null && c.getSdt() != null && !c.getSdt().isBlank()
                && repo.existsBySdtAndIdNot(c.getSdt(), c.getId())) {
            throw new IllegalStateException("So dien thoai da ton tai");
        }
        return repo.save(c);
    }

    @Override
    public Customer findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override public List<Customer> findAll() { return repo.findAll(); }
    @Override public List<Customer> findMine(Long ownerId) { return repo.findByOwnerId(ownerId); }
    @Override public List<Customer> searchAll(String kw) { return repo.searchAll(kw == null ? "" : kw); }
    @Override public List<Customer> searchMine(Long ownerId, String kw) { return repo.searchMine(ownerId, kw == null ? "" : kw); }
    @Override public void delete(Long id) { repo.deleteById(id); }
}

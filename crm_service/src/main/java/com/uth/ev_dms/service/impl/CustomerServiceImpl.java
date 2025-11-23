package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.config.CacheConfig;
import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.repo.CustomerRepo;
import com.uth.ev_dms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo repo;

    // =========================================================
    // ======================== CREATE =========================
    // =========================================================

    @Override
    @Transactional
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.CUSTOMERS_ALL,
                    CacheConfig.CacheNames.CUSTOMERS_BY_OWNER
            },
            allEntries = true
    )
    public Customer create(Customer c) {
        if (c.getSdt() != null && !c.getSdt().isBlank() && repo.existsBySdt(c.getSdt())) {
            throw new IllegalStateException("So dien thoai da ton tai");
        }
        return repo.save(c);
    }

    // =========================================================
    // ======================== UPDATE =========================
    // =========================================================

    @Override
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.CUSTOMERS_ALL,
                    CacheConfig.CacheNames.CUSTOMERS_BY_OWNER
            },
            allEntries = true
    )
    public Customer update(Customer c) {
        if (c.getId() != null && c.getSdt() != null && !c.getSdt().isBlank()
                && repo.existsBySdtAndIdNot(c.getSdt(), c.getId())) {
            throw new IllegalStateException("So dien thoai da ton tai");
        }
        return repo.save(c);
    }

    // =========================================================
    // ========================== GET ==========================
    // =========================================================

    @Override
    @Cacheable(
            value = CacheConfig.CacheNames.CUSTOMERS_ALL,
            key = "#id"
    )
    public Customer findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    @Cacheable(value = CacheConfig.CacheNames.CUSTOMERS_ALL)
    public List<Customer> findAll() {
        return repo.findAll();
    }

    @Override
    @Cacheable(
            value = CacheConfig.CacheNames.CUSTOMERS_BY_OWNER,
            key = "#ownerId"
    )
    public List<Customer> findMine(Long ownerId) {
        return repo.findByOwnerId(ownerId);
    }

    // SEARCH thường không cache (vì dynamic) nhưng nếu bạn muốn → thêm TTL + Redis

    @Override
    public List<Customer> searchAll(String kw) {
        return repo.searchAll(kw == null ? "" : kw);
    }

    @Override
    public List<Customer> searchMine(Long ownerId, String kw) {
        return repo.searchMine(ownerId, kw == null ? "" : kw);
    }

    // =========================================================
    // ========================= DELETE =========================
    // =========================================================

    @Override
    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.CUSTOMERS_ALL,
                    CacheConfig.CacheNames.CUSTOMERS_BY_OWNER
            },
            allEntries = true
    )
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

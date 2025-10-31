package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
    Page<Customer> findByOwnerId(Long ownerId, Pageable pageable);
}


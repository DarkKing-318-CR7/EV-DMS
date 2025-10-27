package com.uth.ev_dms.crm;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
}

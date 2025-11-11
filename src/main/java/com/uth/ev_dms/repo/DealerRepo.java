package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DealerRepo extends JpaRepository<Dealer, Long> {
    boolean existsByCode(String code);
    Optional<Dealer> findByCode(String code);
}

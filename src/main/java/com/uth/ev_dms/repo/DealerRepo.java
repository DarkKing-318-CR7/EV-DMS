package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealerRepo extends JpaRepository<Dealer, Long> { }

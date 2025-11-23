package com.uth.ev_dms.repo;


import com.uth.ev_dms.domain.DealerBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DealerBranchRepo extends JpaRepository<DealerBranch, Long> {
    Optional<DealerBranch> findByDealerId(Long dealerId);
}

package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepo extends JpaRepository<Region, Long> {
}

package com.uth.ev_dms.repo;

import com.uth.ev_dms.domain.InventoryMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryMoveRepo extends JpaRepository<InventoryMove, Long> {
    // Nếu cần filter theo dealer/trim/order thì thêm method ở đây
}

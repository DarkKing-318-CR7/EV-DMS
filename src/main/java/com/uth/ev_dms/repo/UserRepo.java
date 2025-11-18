package com.uth.ev_dms.repo;

import com.uth.ev_dms.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByDealer_Id(Long dealerId);
    List<User> findByDealer_IdAndRoles_Name(Long dealerId, String roleName);


}

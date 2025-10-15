package com.uth.ev_dms.admin.repo;

import com.uth.ev_dms.admin.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndActiveTrue(String username);
}

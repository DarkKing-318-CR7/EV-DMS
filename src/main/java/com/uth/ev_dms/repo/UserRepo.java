package com.uth.ev_dms.repo;

import com.uth.ev_dms.auth.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"dealer", "region"})
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = {"dealer", "region"})
    Optional<User> findByUsername(String username);
}

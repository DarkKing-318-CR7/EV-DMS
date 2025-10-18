package com.uth.ev_dms.repo;

import com.uth.ev_dms.auth.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles") // eager roles
    Optional<User> findByUsername(String username);

    // neu muon dang nhap bang email thi bo sung:
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

}

package com.ev.dms.infrastructure.persistence.jpa.user;

import com.ev.dms.domain.user.User;
import com.ev.dms.domain.user.repository.UserRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
    @Override
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByUsername(String username);
}

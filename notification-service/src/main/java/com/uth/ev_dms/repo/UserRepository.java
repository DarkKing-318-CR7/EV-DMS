package com.uth.ev_dms.repo;

import com.uth.ev_dms.auth.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    @Query("select u.id from User u where u.username = :username")
    Long findIdByUsername(@Param("username") String username);

    // vì entity có field dealer (ManyToOne), lấy id qua u.dealer.id
    @Query("select u.dealer.id from User u where u.username = :username")
    Long findDealerIdByUsername(@Param("username") String username);

}

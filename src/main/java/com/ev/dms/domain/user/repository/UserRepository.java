package com.ev.dms.domain.user.repository;

import com.ev.dms.domain.user.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    User save(User user);
}

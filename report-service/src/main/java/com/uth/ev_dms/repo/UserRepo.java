package com.uth.ev_dms.repo;

import com.uth.ev_dms.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByDealer_Id(Long dealerId);

    List<User> findByDealer_IdAndRoles_Name(Long dealerId, String roleName);

    // ⭐ LẤY FCM TOKEN THEO USER ID
    @Query("select u.fcmToken from User u where u.id = :id")
    String findFcmTokenByUserId(Long id);

    // ⭐ LẤY USER CÓ FCM TOKEN (nếu muốn gửi nhiều user sau này)
    @Query("select u from User u where u.fcmToken is not null")
    List<User> findAllWithFcmToken();
}

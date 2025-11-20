package com.uth.ev_dms.service;

import java.security.Principal;

public interface UserService {

    Long findIdByUsername(String username);

    Long findDealerIdByUsername(String username);

    // tien dung khi co Principal
    Long getUserId(Principal principal);

    Long getDealerId(Principal principal);

    boolean hasRole(Principal principal, String role); // role: "ROLE_DEALER_MANAGER"

    // ⭐ LƯU FCM TOKEN (Mobile/Web gửi lên)
    void updateFcmToken(Long userId, String token);

    // ⭐ LẤY FCM TOKEN THEO USER ID
    String getFcmToken(Long userId);
}

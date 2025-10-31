// src/main/java/com/uth/ev_dms/service/UserService.java
package com.uth.ev_dms.service;

import java.security.Principal;

public interface UserService {
    Long findIdByUsername(String username);
    Long findDealerIdByUsername(String username);

    // tien dung khi co Principal
    Long getUserId(Principal principal);
    Long getDealerId(Principal principal);
    Long findRegionIdByUsername(String username);
    


    boolean hasRole(Principal principal, String role); // role vi du: "ROLE_DEALER_MANAGER"
}

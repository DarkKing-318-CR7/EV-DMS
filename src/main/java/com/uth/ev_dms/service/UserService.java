// src/main/java/com/uth/ev_dms/service/UserService.java
package com.uth.ev_dms.service;

import com.uth.ev_dms.fix.service.dto.UserDto;

import java.security.Principal;

public interface UserService {
    Long findIdByUsername(String username);
    Long findDealerIdByUsername(String username);

    // tien dung khi co Principal
    Long getUserId(Principal principal);
    Long getDealerId(Principal principal);

    


    boolean hasRole(Principal principal, String role); // role vi du: "ROLE_DEALER_MANAGER"

    public UserDto getUserDto(String username);
}

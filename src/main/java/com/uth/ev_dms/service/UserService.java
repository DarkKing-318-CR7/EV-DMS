// src/main/java/com/uth/ev_dms/service/UserService.java
package com.uth.ev_dms.service;

public interface UserService {
    Long findIdByUsername(String username);
    Long findDealerIdByUsername(String username);
}

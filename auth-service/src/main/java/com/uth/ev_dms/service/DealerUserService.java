package com.uth.ev_dms.service;

import com.uth.ev_dms.auth.User;
import java.util.List;

public interface DealerUserService {

    List<User> findStaff(Long dealerId);

    User createStaff(Long dealerId, User user);

    void toggleStaffStatus(Long dealerId, Long staffId);
}

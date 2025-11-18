package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.auth.Role;
import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.repo.DealerRepo;
import com.uth.ev_dms.repo.RoleRepository;
import com.uth.ev_dms.repo.UserRepo;
import com.uth.ev_dms.repo.UserRepository;
import com.uth.ev_dms.service.DealerUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DealerUserServiceImpl implements DealerUserService {

    private final UserRepo userRepo;
    private final DealerRepo dealerRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Override
    public List<User> findStaff(Long dealerId) {
        return userRepo.findByDealer_IdAndRoles_Name(dealerId, "ROLE_DEALER_STAFF");
    }

    @Override
    @Transactional
    public User createStaff(Long dealerId, User user) {

        Dealer dealer = dealerRepo.findById(dealerId)
                .orElseThrow(() -> new IllegalStateException("Dealer not found"));

        user.setDealer(dealer);

        // Role staff đúng
        Role staffRole = roleRepo.findByName("ROLE_DEALER_STAFF");
        if (staffRole == null) {
            throw new IllegalStateException("ROLE_DEALER_STAFF not found");
        }

        user.setRoles(Set.of(staffRole));
        user.setPassword(encoder.encode(user.getPassword()));  // mã hoá pass
        user.setEnabled(true);

        return userRepo.save(user);
    }

    @Override
    @Transactional
    public void toggleStaffStatus(Long dealerId, Long staffId) {

        User u = userRepo.findById(staffId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (u.getDealer() == null || !u.getDealer().getId().equals(dealerId)) {
            throw new IllegalStateException("Cannot modify staff of another dealer");
        }

        u.setEnabled(!u.isEnabled());
        userRepo.save(u);
    }
}

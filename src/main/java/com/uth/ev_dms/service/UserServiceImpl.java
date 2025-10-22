package com.uth.ev_dms.service;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Long findIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public Long findDealerIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getDealer() != null ? u.getDealer().getId() : null)
                .orElse(null);
    }

    @Override
    public Long findRegionIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getRegion() != null ? u.getRegion().getId() : null)
                .orElse(null);
    }

    @Override
    public Long getUserId(Principal principal) {
        if (principal == null) return null;
        return findIdByUsername(principal.getName());
    }

    @Override
    public Long getDealerId(Principal principal) {
        if (principal == null) return null;
        return findDealerIdByUsername(principal.getName());
    }

    @Override
    public boolean hasRole(Principal principal, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        String r1 = role;
        String r2 = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        for (GrantedAuthority ga : auth.getAuthorities()) {
            String g = ga.getAuthority();
            if (g.equalsIgnoreCase(r1) || g.equalsIgnoreCase(r2)) return true;
        }
        return false;
    }
}

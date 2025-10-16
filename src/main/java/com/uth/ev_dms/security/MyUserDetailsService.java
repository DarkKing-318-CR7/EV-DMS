package com.uth.ev_dms.security;

import com.uth.ev_dms.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;
    public MyUserDetailsService(UserRepository userRepo) { this.userRepo = userRepo; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));

        var authorities = u.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName())) // "ROLE_ADMIN"
                .toList();

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),          // BCrypt trong DB
                u.isEnabled(),            // enabled
                true, true, true,
                authorities
        );
    }
}

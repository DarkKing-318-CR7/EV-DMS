package com.uth.ev_dms.security;


import com.uth.ev_dms.admin.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = userRepo.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        var auths = u.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getCode()))
                .toList();

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(), u.getPassword(), auths
        );
    }
}

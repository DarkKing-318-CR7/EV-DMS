package com.uth.ev_dms.service;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
        // Tạm thời chưa có mapping dealer trong User -> trả về null (hoặc ném exception tùy bạn)
        return userRepository.findByUsername(username)
                .map(u -> (Long) null) // TODO: thay bằng u.getDealerId() hoặc u.getDealer().getId() khi có mapping
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

}

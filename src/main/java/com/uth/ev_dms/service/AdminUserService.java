package com.uth.ev_dms.service;

import com.uth.ev_dms.auth.Role;
import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.repo.DealerRepo;
import com.uth.ev_dms.repo.RegionRepo;
import com.uth.ev_dms.repo.RoleRepository;
import com.uth.ev_dms.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepo userRepo;
    private final RoleRepository roleRepository;
    private final DealerRepo dealerRepo;
    private final RegionRepo regionRepo;
    private final PasswordEncoder passwordEncoder;

    // ======================= CREATE USER ===========================
    @Transactional
    public User createUser(String username, String rawPassword, String email,
                           String fullName, String roleName,
                           Long dealerId, Long regionId, boolean enabled) {

        if (userRepo.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password không được trống");
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setEmail(email);
        u.setFullName(fullName);
        u.setEnabled(enabled);

        // Dealer / Region: chỉ gán khi > 0
        if (dealerId != null && dealerId > 0) {
            dealerRepo.findById(dealerId).ifPresent(u::setDealer);
        } else {
            u.setDealer(null);
        }

        if (regionId != null && regionId > 0) {
            regionRepo.findById(regionId).ifPresent(u::setRegion);
        } else {
            u.setRegion(null);
        }

        // Gán role chính (mutable)
        String rn = (roleName == null ? "" : roleName.trim());
        Role role = roleRepository.findByName(rn);
        if (role == null) throw new IllegalArgumentException("Role không tồn tại: " + rn);
        u.setRoles(new HashSet<>(Collections.singleton(role))); // ✅ mutable set

        return userRepo.save(u);
    }

    // ======================= UPDATE USER ===========================
    @Transactional
    public User updateUser(Long id, String email, String fullName,
                           String roleName, Long dealerId, Long regionId, Boolean enabled,
                           String newRawPasswordIfAny) {

        User u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        u.setEmail(email);
        u.setFullName(fullName);
        if (enabled != null) u.setEnabled(enabled);

        // Đổi mật khẩu nếu nhập mới
        if (newRawPasswordIfAny != null && !newRawPasswordIfAny.isBlank()) {
            u.setPassword(passwordEncoder.encode(newRawPasswordIfAny));
        }

        // Cập nhật role (mutable)
        if (roleName != null && !roleName.isBlank()) {
            String rn = roleName.trim();
            Role role = roleRepository.findByName(rn);
            if (role == null) throw new IllegalArgumentException("Role không hợp lệ: " + rn);
            u.setRoles(new HashSet<>(Collections.singleton(role))); // ✅ mutable set
        }

        // Dealer / Region: nếu 0 hoặc null → xóa
        u.setDealer(null);
        if (dealerId != null && dealerId > 0)
            dealerRepo.findById(dealerId).ifPresent(u::setDealer);

        u.setRegion(null);
        if (regionId != null && regionId > 0)
            regionRepo.findById(regionId).ifPresent(u::setRegion);

        return userRepo.save(u);
    }

    // ======================= LIST / GET ===========================
    public List<User> list() {
        return userRepo.findAll();
    }

    public User get(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user id=" + id));
    }
}

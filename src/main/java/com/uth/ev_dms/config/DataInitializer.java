package com.uth.ev_dms.config;

import com.uth.ev_dms.user.Role;
import com.uth.ev_dms.user.RoleRepository;
import com.uth.ev_dms.user.UserEntity;
import com.uth.ev_dms.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepo,
                               RoleRepository roleRepo,
                               PasswordEncoder encoder) {
        return args -> {
            // 🧩 Tạo role nếu chưa có
            List<String> roleNames = List.of("ROLE_ADMIN", "ROLE_DEALER", "ROLE_EVM", "ROLE_USER");
            for (String name : roleNames) {
                if (roleRepo.findByName(name) == null) {
                    roleRepo.save(new Role(name));
                    System.out.println("✅ Created role: " + name);
                }
            }

            // 🧩 Tạo admin mặc định
            if (userRepo.findByUsername("admin").isEmpty()) {
                UserEntity admin = new UserEntity();
                admin.setUsername("admin");
                admin.setEmail("admin@evdms.com");
                admin.setPassword(encoder.encode("123456"));
                admin.setEnabled(true);

                // Gán role ADMIN
                admin.setRoles(new HashSet<>());
                Role adminRole = roleRepo.findByName("ROLE_ADMIN");
                admin.getRoles().add(adminRole);

                userRepo.save(admin);
                System.out.println("✅ Created default admin: admin / 123456");
            }

            // 🧩 Tạo user thường
            if (userRepo.findByUsername("user").isEmpty()) {
                UserEntity user = new UserEntity();
                user.setUsername("user");
                user.setEmail("user@evdms.com");
                user.setPassword(encoder.encode("123456"));
                user.setEnabled(true);
                user.setRoles(new HashSet<>());
                user.getRoles().add(roleRepo.findByName("ROLE_USER"));
                userRepo.save(user);
                System.out.println("✅ Created default user: user / 123456");
            }
        };
    }
}

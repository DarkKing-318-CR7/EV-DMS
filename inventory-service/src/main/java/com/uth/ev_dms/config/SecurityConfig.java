//package com.uth.ev_dms.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableMethodSecurity // để @PreAuthorize hoạt động (nếu bạn dùng)
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        // Mở health/actuator nếu cần
//                        .requestMatchers("/actuator/**","").permitAll()
//
//                        // Tạm thời cho phép tất cả để test (sau này siết lại)
//                        .anyRequest().permitAll()
//                );
//
//        // Nếu sau này dùng JWT Resource Server:
//        // .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
//
//        return http.build();
//    }
//}

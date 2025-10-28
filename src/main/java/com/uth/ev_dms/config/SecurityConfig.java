package com.uth.ev_dms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService myUserDetailsService;

    public SecurityConfig(UserDetailsService myUserDetailsService) {
        this.myUserDetailsService = myUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(myUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) CSRF: bỏ cho các API bạn đang test (để POST không bị 403 do csrf)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/evm/**", "/dealer/**"))

                // 2) Phân quyền chi tiết theo vai trò
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**","/js/**","/image/**","/images/**","/fonts/**","/webjars/**",
                                "/favicon.ico","/login","/error","/error/**"
                        ).permitAll()

                        // Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Dealer Staff xem "Đơn của tôi"
                        .requestMatchers("/dealer/orders/my/**")
                        .hasAnyRole("DEALER_STAFF","DEALER_MANAGER","ADMIN")

                        // ===== Dealer Orders =====
                        // Cho xem chi tiết / list cho cả staff & manager
                        .requestMatchers(HttpMethod.GET, "/dealer/orders/**")
                        .hasAnyRole("DEALER_STAFF","DEALER_MANAGER","ADMIN")

                        // Hành động chỉ Manager (ví dụ duyệt/allocate…)
                        .requestMatchers(HttpMethod.POST, "/dealer/orders/*/allocate")
                        .hasAnyRole("DEALER_MANAGER","ADMIN")

                        // Hủy: cho staff cũng được (nếu nghiệp vụ cho phép)
                        .requestMatchers(HttpMethod.POST, "/dealer/orders/*/cancel")
                        .hasAnyRole("DEALER_STAFF","DEALER_MANAGER","ADMIN")

                        // ===== EVM =====
                        .requestMatchers(HttpMethod.GET,  "/evm/orders/pending")
                        .hasAnyRole("EVM_STAFF","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/evm/orders/*/approve-allocate")
                        .hasAnyRole("EVM_STAFF","ADMIN")
                        .requestMatchers("/evm/orders/**")
                        .hasAnyRole("EVM_STAFF","ADMIN")

                        .anyRequest().authenticated()
                )

                // 3) Trang lỗi quyền
                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"))

                // 4) Login form như cũ
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/post-login", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // 5) Logout như cũ
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // 6) Thêm httpBasic để test API nhanh bằng cURL/Postman
                .httpBasic(Customizer.withDefaults())

                .authenticationProvider(authenticationProvider());

        http.sessionManagement(session -> session.sessionFixation().migrateSession());
        return http.build();
    }
}

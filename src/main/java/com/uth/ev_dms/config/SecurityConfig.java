package com.uth.ev_dms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.uth.ev_dms.security.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService myUserDetailsService;

    public SecurityConfig(UserDetailsService myUserDetailsService) {
        this.myUserDetailsService = myUserDetailsService;
    }

    // BCrypt cho các mật khẩu đã hash trong DB
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Provider dùng UserDetailsService + BCrypt
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
                // Phân quyền URL
                .authorizeHttpRequests(auth -> auth
                        // Cho phép static assets + trang login
                        .requestMatchers(
                                "/css/**", "/js/**", "/image/**", "/fonts/**", "/webjars/**",
                                "/favicon.ico", "/login", "/error"
                        ).permitAll()

                        // Khu vực Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Khu vực Dealer (3 role của đại lý + admin)
                        .requestMatchers("/dealer/**")
                        .hasAnyRole("DEALER_MANAGER","DEALER_STAFF","EVM_STAFF","ADMIN")

                        // Mọi request khác phải đăng nhập
                        .anyRequest().authenticated()
                )

                // Cấu hình form login
                .formLogin(form -> form
                        .loginPage("/login")                 // GET /login -> trang login.html
                        .loginProcessingUrl("/login")        // POST /login -> Spring Security xử lý
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/post-login", true) // ĐIỀU HƯỚNG SAU KHI LOGIN THÀNH CÔNG
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // CSRF vẫn bật (mặc định). Nếu có API thuần JSON thì ignore tại đây.
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )

                // Gắn provider
                .authenticationProvider(authenticationProvider());

        // (tuỳ chọn) kiểm soát session
        http.sessionManagement(session -> session
                .sessionFixation().migrateSession()
        );

        return http.build();
    }
}

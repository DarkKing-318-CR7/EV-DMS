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
                // CSRF: nới lỏng cho các tuyến đang test
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/evm/**", "/dealer/**"))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**","/js/**","/image/**","/images/**","/fonts/**","/webjars/**",
                                "/favicon.ico","/login","/error","/error/**"
                        ).permitAll()

                        // ===== Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ===== Dealer: CHỈ "Đơn của tôi" cho Staff/Manager
                        .requestMatchers("/dealer/orders/my/**")
                        .hasAnyRole("DEALER_STAFF","DEALER_MANAGER","ADMIN")

                        // Trang LIST "Tất cả đơn" CHỈ Manager (dstaff bị chặn)
                        .requestMatchers(HttpMethod.GET, "/dealer/orders", "/dealer/orders/")
                        .hasAnyRole("DEALER_MANAGER","ADMIN")

                        // Trang chi tiết đơn: cho cả Staff/Manager (controller sẽ tự kiểm tra đúng quyền trên từng đơn)
                        .requestMatchers(HttpMethod.GET, "/dealer/orders/*")
                        .hasAnyRole("DEALER_STAFF","DEALER_MANAGER","ADMIN")

                        // Các hành động POST của dealer (tùy nghiệp vụ, có thể siết thêm)
                        .requestMatchers(HttpMethod.POST, "/dealer/orders/*/allocate",
                                "/dealer/orders/*/cancel",
                                "/dealer/orders/*/pay-cash",
                                "/dealer/orders/*/installment",
                                "/dealer/orders/*/request-allocate")
                        .hasAnyRole("DEALER_STAFF","DEALER_MANAGER","ADMIN")

                        // ===== EVM
                        .requestMatchers(HttpMethod.GET,  "/evm/orders/pending").hasAnyRole("EVM_STAFF","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/evm/orders/*/approve-allocate").hasAnyRole("EVM_STAFF","ADMIN")
                        .requestMatchers("/evm/orders/**").hasAnyRole("EVM_STAFF","ADMIN")

                        .requestMatchers("/dealer/dashboard-manager").hasRole("DEALER_MANAGER")
                        .requestMatchers("/dealer/dashboard").hasRole("DEALER_STAFF")


                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"))

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/post-login", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider());

        http.sessionManagement(session -> session.sessionFixation().migrateSession());
        return http.build();
    }
}

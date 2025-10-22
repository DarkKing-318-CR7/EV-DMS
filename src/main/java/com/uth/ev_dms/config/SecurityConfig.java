package com.uth.ev_dms.config;

import com.uth.ev_dms.security.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;

    public SecurityConfig(MyUserDetailsService myUserDetailsService) {
        this.myUserDetailsService = myUserDetailsService;
    }

    // ===== ENCODER =====
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===== AUTH PROVIDER =====
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

    // ===== SECURITY RULES =====
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Static resources + login page
                        .requestMatchers("/css/**", "/js/**", "/image/**", "/login", "/error").permitAll()

                        // ADMIN routes
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Quotes routes (specific first)
                        .requestMatchers("/dealer/quotes/my/**").hasAnyRole("DEALER_STAFF","ADMIN")
                        .requestMatchers("/dealer/quotes/pending/**").hasAnyRole("DEALER_MANAGER","ADMIN")

                        // Dealer module general routes (after quotes)
                        .requestMatchers("/dealer/**").hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "EVM_STAFF", "ADMIN")

                        // Default rule: all must be authenticated
                        .anyRequest().authenticated()
                )

                // Form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/post-login", true) // chuyển hướng theo role
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // Logout config
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                // Provider
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}

package com.uth.ev_dms.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// com.uth.emvdms.security.WebSecurity.java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurity {
    private final UserDetailsServiceImpl uds;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/favicon.ico",
                                "/css/**", "/js/**", "/images/**", "/image/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN","EVM")
                        .requestMatchers("/dealer/**").hasAnyRole("ADMIN","EVM","MANAGER","DEALER")
                        .anyRequest().authenticated()
                )
                .userDetailsService(uds)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/post-login", true)
                        .permitAll()
                )

                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?out").permitAll());
        return http.build();
    }
    // WebSecurity.java
    @Bean
    UserDetailsService inMemoryUsers(PasswordEncoder pe) {
        var admin = org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password(pe.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new org.springframework.security.provisioning.InMemoryUserDetailsManager(admin);
    }

}

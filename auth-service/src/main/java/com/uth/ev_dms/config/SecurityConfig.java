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
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/evm/**", "/dealer/**", "/login"))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**","/js/**","/image/**","/images/**","/fonts/**","/webjars/**",
                                "/favicon.ico","/login","/error","/error/**",
                                "/auth/**"
                        ).permitAll()

                        // ===== Admin =====
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ===== EVM =====
                        .requestMatchers("/evm/**").hasAnyRole("EVM_STAFF", "ADMIN")

                        // ===== Manager =====
                        .requestMatchers("/manager/**").hasAnyRole("DEALER_MANAGER", "ADMIN")

                        // ===== Staff & Dealer =====
                        .requestMatchers("/staff/**").hasAnyRole("DEALER_STAFF", "DEALER_MANAGER", "ADMIN")
                        .requestMatchers("/dealer/**").hasAnyRole("DEALER_STAFF", "DEALER_MANAGER", "ADMIN")

                        // ===== Reports & Support =====
                        .requestMatchers("/reports/**").hasAnyRole("DEALER_MANAGER", "EVM_STAFF", "ADMIN")
                        .requestMatchers("/support/**").authenticated()

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

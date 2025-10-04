package com.megycakes.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain web(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/", "/c/**", "/p/**",
                                         "/cart/**",
                                         "/checkout", "/order/**",
                                         "/assets/**", "/htmx/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .csrf(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
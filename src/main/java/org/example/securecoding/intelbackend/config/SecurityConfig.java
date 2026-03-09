package org.example.securecoding.intelbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * INTENTIONALLY INSECURE Security Configuration.
 *
 * This config permits ALL requests to the shallow API endpoints and the
 * H2 console without any authentication — simulating the lack of backend
 * access controls in the original Intel system.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Permit all shallow API endpoints (intentionally insecure)
                        .requestMatchers("/api/shallow/**").permitAll()
                        // Permit H2 console access
                        .requestMatchers("/h2-console/**").permitAll()
                        // All other requests also permitted for this demo
                        .anyRequest().permitAll())
                // Disable CSRF for API testing
                .csrf(csrf -> csrf.disable())
                // Disable frame options for H2 console (it uses iframes)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()));

        return http.build();
    }
}

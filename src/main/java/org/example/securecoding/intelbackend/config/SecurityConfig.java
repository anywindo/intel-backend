package org.example.securecoding.intelbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.example.securecoding.intelbackend.deep.infrastructure.JwtAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * INTENTIONALLY INSECURE Security Configuration for Shallow, 
 * but SECURE for Deep Models.
 *
 * This configuration demonstrates the contrast between a completely open
 * shallow API and a protected deep API following security best practices.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Permit all shallow API endpoints (intentionally insecure)
                        .requestMatchers("/api/shallow/**").permitAll()
                        // SECURE: Require authentication for ALL deep API endpoints
                        .requestMatchers("/api/deep/**").authenticated()
                        // Permit H2 console access
                        .requestMatchers("/h2-console/**").permitAll()
                        // All other requests permitted for this demo
                        .anyRequest().permitAll())
                // Integrate the JWT Filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Disable CSRF for simplified API testing
                .csrf(AbstractHttpConfigurer::disable)
                // Disable frame options for H2 console
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()));

        return http.build();
    }
}

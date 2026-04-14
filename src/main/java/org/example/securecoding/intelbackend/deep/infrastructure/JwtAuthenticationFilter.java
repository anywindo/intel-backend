package org.example.securecoding.intelbackend.deep.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * DEEP MODEL — Infrastructure: JWT Authentication Filter
 * 
 * This filter bridges the gap between the incoming HTTP request and 
 * Spring Security's SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Extract token from header or body (for simplicity in this demo, we can check a header)
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null && JwtValidator.verify(token)) {
            String userId = JwtValidator.extractUserId(token);
            String role = JwtValidator.extractRole(token);
            
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
            );

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, authorities
            );
            
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}

package org.rocman.candidate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.rocman.candidate.utils.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Log4j2
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;


    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                log.warn("Token parsing error | reason={} | ip={} | token={} |request={} | timestamp={}",
                        e.getMessage(), request.getRemoteAddr(), token, request.getRequestURI(), LocalDateTime.now());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);


            try {
                JwtUtil.TokenValidationStatus status = jwtUtil.validateTokenDetailed(token);
                if (status == JwtUtil.TokenValidationStatus.VALID) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("Authentication successful | user={} | ip={} | timestamp={}",
                            username, request.getRemoteAddr(), LocalDateTime.now());
                } else {
                    log.warn("Authentication failed | reason={} | user={} | ip={} | token={} |request={} | timestamp={}",
                            status, username, request.getRemoteAddr(), token, request.getRequestURI(), LocalDateTime.now());
                }
            } catch (Exception e) {
                log.error("Authentication error during token validation | reason={} | user={} | ip={} | timestamp={}",
                        e.getMessage(), username, request.getRemoteAddr(), LocalDateTime.now());
            }
        } else if (username == null && authHeader != null) {
            log.warn("Unauthorized access attempt blocked | ip={} | request={} | timestamp={}",
                    request.getRemoteAddr(), request.getRequestURI(), LocalDateTime.now());
        }

        filterChain.doFilter(request, response);
    }
}

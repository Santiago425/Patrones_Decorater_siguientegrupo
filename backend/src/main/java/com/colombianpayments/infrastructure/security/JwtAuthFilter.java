package com.colombianpayments.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rejects every API call without a valid bearer token, except login and CORS preflights. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String USER_ATTRIBUTE = "payments.user";
    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final JwtTokenIssuer tokenIssuer;

    public JwtAuthFilter(JwtTokenIssuer tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/")
                || path.equals(LOGIN_PATH)
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        Optional<String> user =
                header != null && header.startsWith("Bearer ")
                        ? tokenIssuer.verify(header.substring(7).trim())
                        : Optional.empty();
        if (user.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Missing, invalid or expired token\"}");
            return;
        }
        request.setAttribute(USER_ATTRIBUTE, user.get());
        chain.doFilter(request, response);
    }
}

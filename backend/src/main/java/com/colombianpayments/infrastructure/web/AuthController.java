package com.colombianpayments.infrastructure.web;

import com.colombianpayments.application.TokenIssuer;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String DEMO_USER = "demo";
    private static final String DEMO_PASSWORD = "demo123";

    private final TokenIssuer tokenIssuer;
    private final Duration ttl;

    public AuthController(TokenIssuer tokenIssuer, @Value("${payments.jwt.ttl-hours}") long ttlHours) {
        this.tokenIssuer = tokenIssuer;
        this.ttl = Duration.ofHours(ttlHours);
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token, Instant expiresAt) {
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (!DEMO_USER.equals(request.username()) || !DEMO_PASSWORD.equals(request.password())) {
            throw new UnauthorizedException("Wrong username or password");
        }
        TokenIssuer.IssuedToken issued = tokenIssuer.issue(request.username(), ttl);
        return new LoginResponse(issued.token(), issued.expiresAt());
    }
}

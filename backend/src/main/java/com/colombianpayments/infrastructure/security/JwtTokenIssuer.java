package com.colombianpayments.infrastructure.security;

import com.colombianpayments.application.TokenIssuer;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Signs and verifies HS256 tokens. Tokens expire, as the course requires. */
@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final SecretKey key;

    public JwtTokenIssuer(@Value("${payments.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public IssuedToken issue(String username, Duration ttl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);
        String token =
                Jwts.builder()
                        .subject(username)
                        .issuedAt(Date.from(now))
                        .expiration(Date.from(expiresAt))
                        .signWith(key)
                        .compact();
        return new IssuedToken(token, expiresAt);
    }

    /** Returns the username when the token is valid and not expired. */
    public Optional<String> verify(String token) {
        try {
            return Optional.of(
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

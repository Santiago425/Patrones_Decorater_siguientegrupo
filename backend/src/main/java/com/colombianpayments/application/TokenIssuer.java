package com.colombianpayments.application;

import java.time.Duration;
import java.time.Instant;

/** Port to the token issuer used by the login endpoint. */
public interface TokenIssuer {

    IssuedToken issue(String username, Duration ttl);

    /** The signed token and the moment it stops being valid. */
    record IssuedToken(String token, Instant expiresAt) {
    }
}
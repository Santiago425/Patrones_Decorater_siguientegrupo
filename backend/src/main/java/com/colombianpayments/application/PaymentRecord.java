package com.colombianpayments.application;

import java.time.Instant;

/** A payment that was created and kept, with the quote it was priced with. */
public record PaymentRecord(
        String id,
        String merchantNit,
        String description,
        PaymentStatus status,
        Instant createdAt,
        Quote quote) {
}
package com.colombianpayments.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Colombian pesos are whole units, so every rate is applied with half up rounding. */
public final class Money {

    private Money() {}

    /** {@code amountCop * rate}, rounded half up. */
    public static long percentageOf(long amountCop, BigDecimal rate) {
        return BigDecimal.valueOf(amountCop).multiply(rate).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
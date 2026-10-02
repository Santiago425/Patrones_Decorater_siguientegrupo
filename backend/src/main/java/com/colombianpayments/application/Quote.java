package com.colombianpayments.application;

import com.colombianpayments.domain.Layer;
import java.util.List;

/** The priced breakdown of a payment, layers ordered from the innermost decorator to the outermost one. */
public record Quote(
        String currency,
        long baseAmountCop,
        long payerTotalCop,
        long merchantNetCop,
        List<Layer> layers) {
}
package com.colombianpayments.domain;

import java.util.List;

/**
 * A payment and what it costs. The base payment has no layers, each decorator wraps one and adds a single
 * {@link Layer}, so the layers read from the innermost charge to the outermost one.
 */
public interface Payment {

    long baseAmountCop();

    /** What the payer pays: the base amount plus the payer borne layers. */
    long payerTotalCop();

    /** What the merchant receives: the base amount plus the merchant borne layers. */
    long merchantNetCop();

    /** Layers from the innermost to the outermost decorator. */
    List<Layer> layers();

    String description();
}
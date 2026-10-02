package com.colombianpayments.domain.option;

import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;

/** A charge or credit the payer can turn on, with the copy shown next to its toggle. */
public interface PaymentOption {

    LayerCode code();

    String name();

    /** What the option is for, in one sentence. */
    String description();

    /** How the amount is calculated, shown next to the toggle. */
    String pricingRule();

    /** Wraps {@code inner} with this option. */
    Payment apply(Payment inner);
}
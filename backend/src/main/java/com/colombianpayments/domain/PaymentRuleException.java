package com.colombianpayments.domain;

/** A business rule of the domain that cannot be satisfied, such as an option that does not apply. */
public class PaymentRuleException extends RuntimeException {

    public PaymentRuleException(String message) {
        super(message);
    }
}
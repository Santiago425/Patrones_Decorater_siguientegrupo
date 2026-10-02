package com.colombianpayments.application;

/** A payment request that breaks one of the business rules. Reported to the client as 400. */
public class InvalidPaymentException extends RuntimeException {

    public InvalidPaymentException(String message) {
        super(message);
    }
}
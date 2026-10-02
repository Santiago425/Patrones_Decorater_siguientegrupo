package com.colombianpayments.domain;

/** The five charge and credit concepts, in the fixed wrapping order of the catalog. */
public enum LayerCode {
    VAT,
    GATEWAY_FEE,
    WITHHOLDING,
    GMF,
    CASHBACK
}
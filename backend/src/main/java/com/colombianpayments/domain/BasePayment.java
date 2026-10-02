package com.colombianpayments.domain;

import java.util.List;
import java.util.Objects;

/** A payment without any charge or credit: the payer and the merchant both move the base amount. */
public class BasePayment implements Payment {

    private final long baseAmountCop;
    private final String description;

    public BasePayment(long baseAmountCop, String description) {
        this.baseAmountCop = baseAmountCop;
        this.description = description;
    }

    @Override
    public long baseAmountCop() {
        return baseAmountCop;
    }

    @Override
    public long payerTotalCop() {
        return baseAmountCop;
    }

    @Override
    public long merchantNetCop() {
        return baseAmountCop;
    }

    @Override
    public List<Layer> layers() {
        return List.of();
    }

    @Override
    public String description() {
        return description;
    }
}
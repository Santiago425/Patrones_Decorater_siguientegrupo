package com.colombianpayments.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wraps a payment by composition and adds one layer. The totals are recomputed on every call from the
 * wrapped payment, so the decorator stack is the single source of truth of the breakdown.
 */
public abstract class PaymentDecorator implements Payment {

    private final Payment inner;
    private Layer added;

    protected PaymentDecorator(Payment inner) {
        this.inner = Objects.requireNonNull(inner, "inner payment is required");
    }

    /** The layer this decorator adds, computed once. */
    protected abstract Layer layer();

    /** The payment being wrapped, for the decorators whose rate depends on the inner layers. */
    protected Payment inner() {
        return inner;
    }

    @Override
    public long baseAmountCop() {
        return inner.baseAmountCop();
    }

    @Override
    public long payerTotalCop() {
        return inner.payerTotalCop() + amountBorneBy(Bearer.PAYER);
    }

    @Override
    public long merchantNetCop() {
        return inner.merchantNetCop() + amountBorneBy(Bearer.MERCHANT);
    }

    @Override
    public List<Layer> layers() {
        List<Layer> all = new ArrayList<>(inner.layers());
        all.add(addedLayer());
        return List.copyOf(all);
    }

    @Override
    public String description() {
        return inner.description();
    }

    private Layer addedLayer() {
        if (added == null) {
            added = layer();
        }
        return added;
    }

    private long amountBorneBy(Bearer bearer) {
        Layer current = addedLayer();
        return current.bearer() == bearer ? current.amountCop() : 0L;
    }
}
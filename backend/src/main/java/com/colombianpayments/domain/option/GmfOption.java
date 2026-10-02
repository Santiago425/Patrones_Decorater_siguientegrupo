package com.colombianpayments.domain.option;

import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.decorator.GmfDecorator;

/** The GMF (4x1000) option of the catalog. */
public class GmfOption implements PaymentOption {

    @Override
    public LayerCode code() {
        return LayerCode.GMF;
    }

    @Override
    public String name() {
        return "GMF (4x1000)";
    }

    @Override
    public String description() {
        return "Financial transaction tax on the accumulated payer total.";
    }

    @Override
    public String pricingRule() {
        return "+0.4% of the payer total accumulated by the inner layers";
    }

    @Override
    public Payment apply(Payment inner) {
        return new GmfDecorator(inner);
    }
}
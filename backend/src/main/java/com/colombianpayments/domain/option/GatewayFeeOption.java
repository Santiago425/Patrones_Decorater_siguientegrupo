package com.colombianpayments.domain.option;

import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.decorator.GatewayFeeDecorator;

/** The gateway fee option of the catalog. */
public class GatewayFeeOption implements PaymentOption {

    @Override
    public LayerCode code() {
        return LayerCode.GATEWAY_FEE;
    }

    @Override
    public String name() {
        return "Gateway fee";
    }

    @Override
    public String description() {
        return "Commission the payment gateway charges on every transaction.";
    }

    @Override
    public String pricingRule() {
        return "-(2.65% of the purchase amount + 900 COP)";
    }

    @Override
    public Payment apply(Payment inner) {
        return new GatewayFeeDecorator(inner);
    }
}
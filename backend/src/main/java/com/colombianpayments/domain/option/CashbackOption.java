package com.colombianpayments.domain.option;

import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.decorator.CashbackDecorator;

/** The cashback option of the catalog. */
public class CashbackOption implements PaymentOption {

    @Override
    public LayerCode code() {
        return LayerCode.CASHBACK;
    }

    @Override
    public String name() {
        return "Cashback";
    }

    @Override
    public String description() {
        return "Promotional credit returned to the payer.";
    }

    @Override
    public String pricingRule() {
        return "-1% of the purchase amount, up to 20000 COP";
    }

    @Override
    public Payment apply(Payment inner) {
        return new CashbackDecorator(inner);
    }
}
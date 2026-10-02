package com.colombianpayments.domain.option;

import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.decorator.VatDecorator;
import java.math.BigDecimal;

/** The VAT option of the catalog. */
public class VatOption implements PaymentOption {

    @Override
    public LayerCode code() {
        return LayerCode.VAT;
    }

    @Override
    public String name() {
        return "VAT (IVA)";
    }

    @Override
    public String description() {
        return "Sales tax charged on the purchase amount.";
    }

    @Override
    public String pricingRule() {
        return "+19% of the purchase amount";
    }

    @Override
    public Payment apply(Payment inner) {
        return new VatDecorator(inner);
    }

    static BigDecimal rate() {
        return VatDecorator.RATE;
    }
}
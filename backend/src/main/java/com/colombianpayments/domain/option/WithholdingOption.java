package com.colombianpayments.domain.option;

import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.decorator.WithholdingDecorator;

/** The withholding tax option of the catalog, with the UVT value used for its threshold. */
public class WithholdingOption implements PaymentOption {

    private final long uvtCop;

    public WithholdingOption(long uvtCop) {
        this.uvtCop = uvtCop;
    }

    @Override
    public LayerCode code() {
        return LayerCode.WITHHOLDING;
    }

    @Override
    public String name() {
        return "Withholding tax (retefuente)";
    }

    @Override
    public String description() {
        return "Tax withheld from the merchant settlement on large amounts.";
    }

    @Override
    public String pricingRule() {
        return "-2.5% of the purchase amount, from " + WithholdingDecorator.MIN_UVT + " UVT";
    }

    @Override
    public Payment apply(Payment inner) {
        return new WithholdingDecorator(inner, uvtCop);
    }
}
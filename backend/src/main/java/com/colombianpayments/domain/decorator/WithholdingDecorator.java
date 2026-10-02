package com.colombianpayments.domain.decorator;

import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Money;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.PaymentDecorator;
import com.colombianpayments.domain.PaymentRuleException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Withholding tax (retefuente): 2.5% of the base amount, taken from the merchant settlement. It only
 * applies from 27 UVT, so below that threshold the option is rejected instead of silently ignored.
 */
public class WithholdingDecorator extends PaymentDecorator {

    public static final BigDecimal RATE = new BigDecimal("0.025");
    public static final int MIN_UVT = 27;

    private final long minimumBaseCop;

    public WithholdingDecorator(Payment inner, long uvtCop) {
        super(inner);
        this.minimumBaseCop = MIN_UVT * uvtCop;
        requireEligibleAmount();
    }

    @Override
    protected Layer layer() {
        long amountCop = -Money.percentageOf(baseAmountCop(), RATE);
        return new Layer(LayerCode.WITHHOLDING, "Withholding tax (retefuente)", amountCop, Bearer.MERCHANT,
                List.of(
                        "2.5% of the purchase amount.",
                        "Applies from " + MIN_UVT + " UVT (" + minimumBaseCop + " COP)."));
    }

    private void requireEligibleAmount() {
        if (baseAmountCop() < minimumBaseCop) {
            throw new PaymentRuleException(
                    "WITHHOLDING applies from " + MIN_UVT + " UVT (" + minimumBaseCop + " COP)");
        }
    }
}
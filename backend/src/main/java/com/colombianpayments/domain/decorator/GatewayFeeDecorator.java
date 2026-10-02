package com.colombianpayments.domain.decorator;

import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Money;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.PaymentDecorator;
import java.math.BigDecimal;
import java.util.List;

/** Gateway fee: 2.65% of the base amount plus 900 COP, taken from the merchant settlement. */
public class GatewayFeeDecorator extends PaymentDecorator {

    public static final BigDecimal RATE = new BigDecimal("0.0265");
    public static final long FIXED_COP = 900L;

    public GatewayFeeDecorator(Payment inner) {
        super(inner);
    }

    @Override
    protected Layer layer() {
        long amountCop = -(Money.percentageOf(baseAmountCop(), RATE) + FIXED_COP);
        return new Layer(LayerCode.GATEWAY_FEE, "Gateway fee", amountCop, Bearer.MERCHANT,
                List.of(
                        "2.65% of the purchase amount plus 900 COP per transaction.",
                        "Deducted from what the merchant receives."));
    }
}
package com.colombianpayments.domain.decorator;

import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Money;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.PaymentDecorator;
import java.math.BigDecimal;
import java.util.List;

/**
 * Cashback: a credit to the payer of 1% of the base amount, up to 20000 COP. It is wrapped last, so it
 * never changes the base GMF is calculated on.
 */
public class CashbackDecorator extends PaymentDecorator {

    public static final BigDecimal RATE = new BigDecimal("0.01");
    public static final long CAP_COP = 20_000L;

    public CashbackDecorator(Payment inner) {
        super(inner);
    }

    @Override
    protected Layer layer() {
        long credit = Math.min(Money.percentageOf(baseAmountCop(), RATE), CAP_COP);
        return new Layer(LayerCode.CASHBACK, "Cashback", -credit, Bearer.PAYER,
                List.of(
                        "1% of the purchase amount, up to " + CAP_COP + " COP.",
                        "Credit to the payer, applied after GMF so the tax is not reduced."));
    }
}
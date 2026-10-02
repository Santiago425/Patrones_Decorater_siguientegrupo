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
 * GMF (4x1000): 0.4% charged to the payer on the payer total accumulated by the decorators it wraps.
 * It is the only order sensitive layer, which is why the catalog always wraps it after VAT.
 */
public class GmfDecorator extends PaymentDecorator {

    public static final BigDecimal RATE = new BigDecimal("0.004");

    public GmfDecorator(Payment inner) {
        super(inner);
    }

    @Override
    protected Layer layer() {
        long amountCop = Money.percentageOf(inner().payerTotalCop(), RATE);
        return new Layer(LayerCode.GMF, "GMF (4x1000)", amountCop, Bearer.PAYER,
                List.of(
                        "0.4% of the payer total accumulated by the inner layers (base amount plus VAT).",
                        "Added to what the payer pays."));
    }
}
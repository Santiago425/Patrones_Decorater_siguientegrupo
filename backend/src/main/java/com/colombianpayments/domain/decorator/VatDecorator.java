package com.colombianpayments.domain.decorator;

import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Money;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.PaymentDecorator;
import java.math.BigDecimal;
import java.util.List;

/** VAT (IVA): 19% of the base amount, charged to the payer. */
public class VatDecorator extends PaymentDecorator {

    public static final BigDecimal RATE = new BigDecimal("0.19");

    public VatDecorator(Payment inner) {
        super(inner);
    }

    @Override
    protected Layer layer() {
        long amountCop = Money.percentageOf(baseAmountCop(), RATE);
        return new Layer(LayerCode.VAT, "VAT (IVA)", amountCop, Bearer.PAYER,
                List.of("19% of the purchase amount.", "Added to what the payer pays."));
    }
}
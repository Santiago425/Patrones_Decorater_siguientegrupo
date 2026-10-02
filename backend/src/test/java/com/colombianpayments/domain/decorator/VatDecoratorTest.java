package com.colombianpayments.domain.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.colombianpayments.domain.BasePayment;
import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VatDecoratorTest {

    @Test
    @DisplayName("VAT adds 19% of the base amount to what the payer pays")
    void addsNineteenPercentToThePayer() {
        Payment payment = new VatDecorator(new BasePayment(1_000_000L, "Sneakers"));

        assertEquals(1_190_000L, payment.payerTotalCop());
    }

    @Test
    @DisplayName("VAT never changes what the merchant receives")
    void leavesTheMerchantUntouched() {
        Payment payment = new VatDecorator(new BasePayment(1_000_000L, "Sneakers"));

        assertEquals(1_000_000L, payment.baseAmountCop());
        assertEquals(1_000_000L, payment.merchantNetCop());
        assertEquals("Sneakers", payment.description());
    }

    @Test
    @DisplayName("VAT exposes one layer borne by the payer")
    void exposesOnePayerLayer() {
        Payment payment = new VatDecorator(new BasePayment(1_000_000L, "Sneakers"));

        Layer layer = payment.layers().get(0);
        assertEquals(1, payment.layers().size());
        assertEquals(LayerCode.VAT, layer.code());
        assertEquals(190_000L, layer.amountCop());
        assertEquals(Bearer.PAYER, layer.bearer());
    }

    @Test
    @DisplayName("VAT rounds half up")
    void roundsHalfUp() {
        Payment payment = new VatDecorator(new BasePayment(1_005L, "Coffee"));

        assertEquals(1_196L, payment.payerTotalCop());
        assertEquals(191L, payment.layers().get(0).amountCop());
    }
}
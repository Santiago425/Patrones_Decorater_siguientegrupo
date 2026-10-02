package com.colombianpayments.domain.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.colombianpayments.domain.BasePayment;
import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CashbackDecoratorTest {

    @Test
    @DisplayName("cashback credits 1% of the base amount to the payer")
    void creditsOnePercentToThePayer() {
        Payment payment = new CashbackDecorator(new BasePayment(1_000_000L, "Sneakers"));

        assertEquals(990_000L, payment.payerTotalCop());
    }

    @Test
    @DisplayName("cashback never changes what the merchant receives")
    void leavesTheMerchantUntouched() {
        Payment payment = new CashbackDecorator(new BasePayment(1_000_000L, "Sneakers"));

        assertEquals(1_000_000L, payment.merchantNetCop());
    }

    @Test
    @DisplayName("cashback is a negative layer borne by the payer")
    void exposesOneNegativePayerLayer() {
        Payment payment = new CashbackDecorator(new BasePayment(1_000_000L, "Sneakers"));

        Layer layer = payment.layers().get(0);
        assertEquals(1, payment.layers().size());
        assertEquals(LayerCode.CASHBACK, layer.code());
        assertEquals(-10_000L, layer.amountCop());
        assertEquals(Bearer.PAYER, layer.bearer());
    }

    @Test
    @DisplayName("cashback is capped at 20000 COP")
    void capsTheCreditAtTwentyThousand() {
        Payment payment = new CashbackDecorator(new BasePayment(3_000_000L, "Sneakers"));

        assertEquals(-20_000L, payment.layers().get(0).amountCop());
        assertEquals(2_980_000L, payment.payerTotalCop());
    }

    @Test
    @DisplayName("cashback pays the whole cap on the amount that reaches it")
    void paysTheFullCapOnTheExactAmount() {
        Payment payment = new CashbackDecorator(new BasePayment(2_000_000L, "Sneakers"));

        assertEquals(-20_000L, payment.layers().get(0).amountCop());
    }

    @Test
    @DisplayName("cashback is charged on the base amount, not on the layers it wraps")
    void ignoresTheWrappedLayers() {
        Payment payment = new CashbackDecorator(new VatDecorator(new BasePayment(1_000_000L, "Sneakers")));

        assertEquals(-10_000L, payment.layers().get(1).amountCop());
    }

    @Test
    @DisplayName("cashback rounds half up")
    void roundsHalfUp() {
        Payment payment = new CashbackDecorator(new BasePayment(150L, "Coffee"));

        assertEquals(-2L, payment.layers().get(0).amountCop());
    }
}
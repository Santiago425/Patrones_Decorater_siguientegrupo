package com.colombianpayments.domain.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.colombianpayments.domain.BasePayment;
import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GatewayFeeDecoratorTest {

    @Test
    @DisplayName("the gateway fee is 2.65% of the base amount plus 900 COP, taken from the merchant")
    void takesCommissionAndFixedFeeFromTheMerchant() {
        Payment payment = new GatewayFeeDecorator(new BasePayment(1_000_000L, "Sneakers"));

        assertEquals(1_000_000L - 27_400L, payment.merchantNetCop());
    }

    @Test
    @DisplayName("the gateway fee never changes what the payer pays")
    void leavesThePayerUntouched() {
        Payment payment = new GatewayFeeDecorator(new BasePayment(1_000_000L, "Sneakers"));

        assertEquals(1_000_000L, payment.baseAmountCop());
        assertEquals(1_000_000L, payment.payerTotalCop());
    }

    @Test
    @DisplayName("the gateway fee is a negative layer borne by the merchant")
    void exposesOneNegativeMerchantLayer() {
        Payment payment = new GatewayFeeDecorator(new BasePayment(1_000_000L, "Sneakers"));

        Layer layer = payment.layers().get(0);
        assertEquals(1, payment.layers().size());
        assertEquals(LayerCode.GATEWAY_FEE, layer.code());
        assertEquals(-27_400L, layer.amountCop());
        assertEquals(Bearer.MERCHANT, layer.bearer());
    }

    @Test
    @DisplayName("the gateway fee rounds the percentage half up and then adds the fixed part")
    void roundsHalfUpBeforeAddingTheFixedPart() {
        Payment payment = new GatewayFeeDecorator(new BasePayment(10_000L, "Coffee"));

        assertEquals(-1_165L, payment.layers().get(0).amountCop());
    }

    @Test
    @DisplayName("the gateway fee ignores the layers added by the decorators it wraps")
    void isAlwaysChargedOnTheBaseAmount() {
        Payment payment = new GatewayFeeDecorator(new VatDecorator(new BasePayment(1_000_000L, "Sneakers")));

        assertEquals(-27_400L, payment.layers().get(1).amountCop());
    }
}
package com.colombianpayments.domain.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.colombianpayments.domain.BasePayment;
import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.PaymentRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WithholdingDecoratorTest {

    private static final long UVT_COP = 52_374L;
    private static final long MINIMUM_BASE_COP = WithholdingDecorator.MIN_UVT * UVT_COP;

    @Test
    @DisplayName("withholding takes 2.5% of the base amount from the merchant")
    void takesTwoAndAHalfPercentFromTheMerchant() {
        Payment payment = new WithholdingDecorator(new BasePayment(2_000_000L, "Sneakers"), UVT_COP);

        assertEquals(1_950_000L, payment.merchantNetCop());
    }

    @Test
    @DisplayName("withholding never changes what the payer pays")
    void leavesThePayerUntouched() {
        Payment payment = new WithholdingDecorator(new BasePayment(2_000_000L, "Sneakers"), UVT_COP);

        assertEquals(2_000_000L, payment.baseAmountCop());
        assertEquals(2_000_000L, payment.payerTotalCop());
    }

    @Test
    @DisplayName("withholding is a negative layer borne by the merchant")
    void exposesOneNegativeMerchantLayer() {
        Payment payment = new WithholdingDecorator(new BasePayment(2_000_000L, "Sneakers"), UVT_COP);

        Layer layer = payment.layers().get(0);
        assertEquals(1, payment.layers().size());
        assertEquals(LayerCode.WITHHOLDING, layer.code());
        assertEquals(-50_000L, layer.amountCop());
        assertEquals(Bearer.MERCHANT, layer.bearer());
    }

    @Test
    @DisplayName("withholding applies on the exact threshold amount")
    void appliesFromTheThresholdInclusive() {
        Payment payment = new WithholdingDecorator(new BasePayment(MINIMUM_BASE_COP, "Sneakers"), UVT_COP);

        assertEquals(1_378_746L, payment.merchantNetCop());
        assertEquals(-35_352L, payment.layers().get(0).amountCop());
    }

    @Test
    @DisplayName("withholding is rejected below 27 UVT and says what the threshold is")
    void rejectsAmountsBelowTheThreshold() {
        Payment inner = new BasePayment(MINIMUM_BASE_COP - 1, "Sneakers");

        PaymentRuleException failure =
                assertThrows(PaymentRuleException.class, () -> new WithholdingDecorator(inner, UVT_COP));

        assertEquals("WITHHOLDING applies from 27 UVT (" + MINIMUM_BASE_COP + " COP)", failure.getMessage());
    }

    @Test
    @DisplayName("the withholding threshold follows the configured UVT value")
    void thresholdFollowsTheConfiguredUvt() {
        long smallUvt = 1_000L;
        long smallThreshold = WithholdingDecorator.MIN_UVT * smallUvt;
        Payment inner = new BasePayment(smallThreshold, "Sneakers");

        Payment payment = new WithholdingDecorator(inner, smallUvt);

        assertEquals(LayerCode.WITHHOLDING, payment.layers().get(0).code());
        assertTrue(payment.merchantNetCop() < smallThreshold);
    }

    @Test
    @DisplayName("withholding rounds half up")
    void roundsHalfUp() {
        Payment payment = new WithholdingDecorator(new BasePayment(MINIMUM_BASE_COP + 2, "Sneakers"), UVT_COP);

        assertEquals(-35_353L, payment.layers().get(0).amountCop());
    }
}
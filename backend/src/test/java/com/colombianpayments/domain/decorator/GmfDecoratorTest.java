package com.colombianpayments.domain.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.colombianpayments.domain.BasePayment;
import com.colombianpayments.domain.Bearer;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GmfDecoratorTest {

    @Test
    @DisplayName("GMF is 0.4% of the payer total accumulated by the layers it wraps")
    void appliesToTheAccumulatedPayerTotal() {
        Payment payment = new GmfDecorator(new VatDecorator(new BasePayment(2_000_000L, "Sneakers")));

        assertEquals(9_520L, payment.layers().get(1).amountCop());
        assertEquals(2_389_520L, payment.payerTotalCop());
    }

    @Test
    @DisplayName("GMF without inner layers falls back to the base amount")
    void appliesToTheBaseAmountAlone() {
        Payment payment = new GmfDecorator(new BasePayment(2_000_000L, "Sneakers"));

        assertEquals(8_000L, payment.payerTotalCop() - payment.baseAmountCop());
    }

    @Test
    @DisplayName("GMF never changes what the merchant receives")
    void leavesTheMerchantUntouched() {
        Payment payment = new GmfDecorator(new VatDecorator(new BasePayment(2_000_000L, "Sneakers")));

        assertEquals(2_000_000L, payment.merchantNetCop());
    }

    @Test
    @DisplayName("GMF is a positive layer borne by the payer")
    void exposesOnePayerLayer() {
        Payment payment = new GmfDecorator(new BasePayment(2_000_000L, "Sneakers"));

        Layer layer = payment.layers().get(0);
        assertEquals(1, payment.layers().size());
        assertEquals(LayerCode.GMF, layer.code());
        assertEquals(8_000L, layer.amountCop());
        assertEquals(Bearer.PAYER, layer.bearer());
    }

    @Test
    @DisplayName("GMF rounds half up")
    void roundsHalfUp() {
        Payment payment = new GmfDecorator(new BasePayment(125L, "Coffee"));

        assertEquals(1L, payment.layers().get(0).amountCop());
    }

    @Test
    @DisplayName("GMF depends on its place in the stack, so wrapping VAT is not the same as being wrapped by it")
    void dependsOnTheWrappingOrder() {
        long baseAmountCop = 2_000_000L;

        Payment gmfOutside =
                new GmfDecorator(new VatDecorator(new BasePayment(baseAmountCop, "Sneakers")));
        Payment gmfInside =
                new VatDecorator(new GmfDecorator(new BasePayment(baseAmountCop, "Sneakers")));

        assertEquals(2_389_520L, gmfOutside.payerTotalCop());
        assertEquals(2_388_000L, gmfInside.payerTotalCop());
    }
}
package com.colombianpayments.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import com.colombianpayments.domain.decorator.CashbackDecorator;
import com.colombianpayments.domain.decorator.GatewayFeeDecorator;
import com.colombianpayments.domain.decorator.GmfDecorator;
import com.colombianpayments.domain.decorator.VatDecorator;
import com.colombianpayments.domain.decorator.WithholdingDecorator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentDecoratorTest {

    @Test
    @DisplayName("the stack lists the layers from the innermost decorator to the outermost one")
    void listsLayersInnermostFirst() {
        Payment payment =
                new CashbackDecorator(new GmfDecorator(new GatewayFeeDecorator(new VatDecorator(
                        new BasePayment(2_000_000L, "Sneakers")))));

        assertIterableEquals(
                List.of(LayerCode.VAT, LayerCode.GATEWAY_FEE, LayerCode.GMF, LayerCode.CASHBACK),
                payment.layers().stream().map(Layer::code).toList());
    }

    @Test
    @DisplayName("the worked example: base 2000000 with VAT, gateway fee and cashback")
    void workedExampleWithoutWithholding() {
        Payment payment =
                new CashbackDecorator(new GmfDecorator(new GatewayFeeDecorator(new VatDecorator(
                        new BasePayment(2_000_000L, "Sneakers")))));

        assertEquals(2_369_520L, payment.payerTotalCop());
        assertEquals(1_946_100L, payment.merchantNetCop());
    }

    @Test
    @DisplayName("the worked example: the full five layer stack on base 2000000")
    void workedExampleWithEveryLayer() {
        Payment payment = fullStack();

        assertEquals(2_000_000L, payment.baseAmountCop());
        assertEquals(2_369_520L, payment.payerTotalCop());
        assertEquals(1_896_100L, payment.merchantNetCop());
    }

    private Payment fullStack() {
        return new CashbackDecorator(new GmfDecorator(new WithholdingDecorator(
                new GatewayFeeDecorator(new VatDecorator(new BasePayment(2_000_000L, "Sneakers"))), 52_374L)));
    }
}
package com.colombianpayments.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BasePaymentTest {

    private final Payment payment = new BasePayment(2_000_000L, "Sneakers");

    @Test
    @DisplayName("a bare payment charges and pays the base amount")
    void baseAmountIsBothTotals() {
        assertEquals(2_000_000L, payment.baseAmountCop());
        assertEquals(2_000_000L, payment.payerTotalCop());
        assertEquals(2_000_000L, payment.merchantNetCop());
    }

    @Test
    @DisplayName("a bare payment has no layers and keeps its description")
    void barePaymentHasNoLayers() {
        assertEquals(List.of(), payment.layers());
        assertEquals("Sneakers", payment.description());
    }
}
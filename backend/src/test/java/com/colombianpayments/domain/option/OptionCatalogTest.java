package com.colombianpayments.domain.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.colombianpayments.domain.BasePayment;
import com.colombianpayments.domain.Layer;
import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.PaymentRuleException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OptionCatalogTest {

    private static final long UVT_COP = 52_374L;

    private final OptionCatalog catalog = OptionCatalog.withUvt(UVT_COP);

    @Test
    @DisplayName("the catalog exposes the five options in wrapping order")
    void exposesTheOptionsInWrappingOrder() {
        assertIterableEquals(
                List.of(LayerCode.VAT, LayerCode.GATEWAY_FEE, LayerCode.WITHHOLDING, LayerCode.GMF, LayerCode.CASHBACK),
                catalog.options().stream().map(PaymentOption::code).toList());
    }

    @Test
    @DisplayName("every option describes itself for the toggles of the form")
    void everyOptionIsDescribed() {
        for (PaymentOption option : catalog.options()) {
            assertNotNull(option.name(), option.code() + " has no name");
            assertTrue(!option.name().isBlank(), option.code() + " has a blank name");
            assertTrue(!option.description().isBlank(), option.code() + " has a blank description");
            assertTrue(!option.pricingRule().isBlank(), option.code() + " has a blank pricing rule");
        }
    }

    @Test
    @DisplayName("the worked example: base 2000000 with the five options")
    void buildsTheWorkedExample() {
        Payment payment = catalog.decorate(
                new BasePayment(2_000_000L, "Sneakers"),
                List.of("VAT", "GATEWAY_FEE", "WITHHOLDING", "GMF", "CASHBACK"));

        assertIterableEquals(
                List.of(LayerCode.VAT, LayerCode.GATEWAY_FEE, LayerCode.WITHHOLDING, LayerCode.GMF, LayerCode.CASHBACK),
                payment.layers().stream().map(layer -> layer.code()).toList());
        assertEquals(2_369_520L, payment.payerTotalCop());
        assertEquals(1_896_100L, payment.merchantNetCop());
    }

    @Test
    @DisplayName("the order of the requested codes does not matter")
    void requestedOrderDoesNotMatter() {
        Payment reversed = catalog.decorate(
                new BasePayment(2_000_000L, "Sneakers"),
                List.of("CASHBACK", "GMF", "WITHHOLDING", "GATEWAY_FEE", "VAT"));

        assertEquals(2_369_520L, reversed.payerTotalCop());
        assertEquals(1_896_100L, reversed.merchantNetCop());
    }

    @Test
    @DisplayName("a shuffled request produces the same stack every time")
    void shuffledRequestsAreEquivalent() {
        List<String> codes =
                List.of("VAT", "GATEWAY_FEE", "WITHHOLDING", "GMF", "CASHBACK");

        for (int seed = 0; seed < 20; seed++) {
            List<String> shuffled = new ArrayList<>(codes);
            Collections.shuffle(shuffled, new Random(seed));

            Payment payment = catalog.decorate(new BasePayment(2_000_000L, "Sneakers"), shuffled);

            assertEquals(2_369_520L, payment.payerTotalCop(), "seed " + seed);
            assertEquals(1_896_100L, payment.merchantNetCop(), "seed " + seed);
        }
    }

    @Test
    @DisplayName("duplicate codes are ignored")
    void ignoresDuplicateCodes() {
        Payment payment = catalog.decorate(
                new BasePayment(2_000_000L, "Sneakers"), List.of("VAT", "VAT", "GMF", "GMF"));

        assertIterableEquals(List.of(LayerCode.VAT, LayerCode.GMF), payment.layers().stream()
                .map(layer -> layer.code()).toList());
        assertEquals(2_389_520L, payment.payerTotalCop());
    }

    @Test
    @DisplayName("no requested option leaves the bare payment untouched")
    void withoutOptionsThePaymentIsBare() {
        Payment payment = catalog.decorate(new BasePayment(2_000_000L, "Sneakers"), List.of());

        assertEquals(List.of(), payment.layers());
        assertEquals(2_000_000L, payment.payerTotalCop());
        assertEquals(2_000_000L, payment.merchantNetCop());
    }

    @Test
    @DisplayName("a missing option list is treated as no options")
    void toleratesAMissingOptionList() {
        Payment payment = catalog.decorate(new BasePayment(2_000_000L, "Sneakers"), null);

        assertEquals(2_000_000L, payment.payerTotalCop());
    }

    @Test
    @DisplayName("an unknown code is rejected")
    void rejectsAnUnknownCode() {
        PaymentRuleException failure = assertThrows(
                PaymentRuleException.class,
                () -> catalog.decorate(new BasePayment(2_000_000L, "Sneakers"), List.of("VAT", "SST")));

        assertEquals("Unknown option code: SST", failure.getMessage());
    }

    @Test
    @DisplayName("withholding below 27 UVT is rejected through the catalog")
    void rejectsWithholdingBelowTheThreshold() {
        PaymentRuleException failure = assertThrows(
                PaymentRuleException.class,
                () -> catalog.decorate(new BasePayment(1_414_097L, "Sneakers"), List.of("WITHHOLDING")));

        assertEquals("WITHHOLDING applies from 27 UVT (1414098 COP)", failure.getMessage());
    }

    @Test
    @DisplayName("the withholding threshold follows the UVT value given to the catalog")
    void thresholdFollowsTheConfiguredUvt() {
        OptionCatalog cheapUvt = OptionCatalog.withUvt(1_000L);

        Payment payment = cheapUvt.decorate(new BasePayment(27_000L, "Sneakers"), List.of("WITHHOLDING"));

        assertEquals(27_000L - 675L, payment.merchantNetCop());
    }
}
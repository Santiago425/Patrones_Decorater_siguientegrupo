package com.colombianpayments.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.option.OptionCatalog;
import com.colombianpayments.infrastructure.persistence.InMemoryPaymentsRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    private static final List<String> ALL_OPTIONS =
            List.of("VAT", "GATEWAY_FEE", "WITHHOLDING", "GMF", "CASHBACK");

    private final InMemoryPaymentsRepository repository = new InMemoryPaymentsRepository();
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:30:00Z"), ZoneOffset.UTC);
    private final PaymentService service = new PaymentService(OptionCatalog.withUvt(52_374L), repository, clock);

    @Test
    @DisplayName("a quote is priced in COP over the decorated payment")
    void quotesTheWorkedExample() {
        Quote quote = service.quote(command(2_000_000L, ALL_OPTIONS));

        assertEquals("COP", quote.currency());
        assertEquals(2_000_000L, quote.baseAmountCop());
        assertEquals(2_369_520L, quote.payerTotalCop());
        assertEquals(1_896_100L, quote.merchantNetCop());
        assertEquals(
                List.of(LayerCode.VAT, LayerCode.GATEWAY_FEE, LayerCode.WITHHOLDING, LayerCode.GMF, LayerCode.CASHBACK),
                quote.layers().stream().map(layer -> layer.code()).toList());
    }

    @Test
    @DisplayName("quoting does not create a payment")
    void quotingCreatesNothing() {
        service.quote(command(2_000_000L, ALL_OPTIONS));

        assertEquals(List.of(), repository.findAllNewestFirst());
    }

    @Test
    @DisplayName("creating an approved payment stores its quote")
    void createStoresAnApprovedPayment() {
        PaymentRecord record = service.create(command(2_000_000L, ALL_OPTIONS));

        assertNotNull(record.id());
        assertTrue(!record.id().isBlank());
        assertEquals(PaymentStatus.APPROVED, record.status());
        assertEquals("800197268-4", record.merchantNit());
        assertEquals("Sneakers", record.description());
        assertEquals(clock.instant(), record.createdAt());
        assertEquals(2_369_520L, record.quote().payerTotalCop());
        assertEquals(1, repository.findAllNewestFirst().size());
    }

    @Test
    @DisplayName("the payment list is newest first")
    void listsPaymentsNewestFirst() {
        PaymentRecord first = service.create(command(2_000_000L, ALL_OPTIONS));
        PaymentRecord second = service.create(command(3_000_000L, ALL_OPTIONS));
        PaymentRecord third = service.create(command(4_000_000L, ALL_OPTIONS));

        List<PaymentRecord> payments = service.list();

        assertEquals(
                List.of(third.id(), second.id(), first.id()),
                payments.stream().map(PaymentRecord::id).toList());
    }

    @Test
    @DisplayName("an unknown option code is a client error")
    void rejectsAnUnknownOption() {
        InvalidPaymentException failure =
                assertThrows(InvalidPaymentException.class, () -> service.quote(command(2_000_000L, List.of("SST"))));

        assertEquals("Unknown option code: SST", failure.getMessage());
    }

    @Test
    @DisplayName("withholding below 27 UVT is a client error")
    void rejectsWithholdingBelowTheThreshold() {
        InvalidPaymentException failure = assertThrows(
                InvalidPaymentException.class, () -> service.quote(command(1_414_097L, List.of("WITHHOLDING"))));

        assertEquals("WITHHOLDING applies from 27 UVT (1414098 COP)", failure.getMessage());
    }

    @Test
    @DisplayName("a rejected payment is not stored")
    void rejectedPaymentIsNotStored() {
        assertThrows(InvalidPaymentException.class, () -> service.create(command(1_000L, List.of("WITHHOLDING"))));

        assertEquals(List.of(), repository.findAllNewestFirst());
    }

    @Test
    @DisplayName("two payments never share an id")
    void idsAreUnique() {
        PaymentRecord first = service.create(command(2_000_000L, ALL_OPTIONS));
        PaymentRecord second = service.create(command(2_000_000L, ALL_OPTIONS));

        assertTrue(!first.id().equals(second.id()));
    }

    private QuoteCommand command(long amountCop, List<String> options) {
        return new QuoteCommand(amountCop, "800197268-4", "Sneakers", options);
    }
}
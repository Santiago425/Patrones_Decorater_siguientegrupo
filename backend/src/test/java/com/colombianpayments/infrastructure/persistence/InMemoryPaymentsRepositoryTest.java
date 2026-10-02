package com.colombianpayments.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.colombianpayments.application.PaymentRecord;
import com.colombianpayments.application.PaymentStatus;
import com.colombianpayments.application.Quote;
import com.colombianpayments.domain.BasePayment;
import com.colombianpayments.domain.Payment;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryPaymentsRepositoryTest {

    private final InMemoryPaymentsRepository repository = new InMemoryPaymentsRepository();

    @Test
    @DisplayName("a fresh repository has no payments")
    void startsEmpty() {
        assertEquals(List.of(), repository.findAllNewestFirst());
    }

    @Test
    @DisplayName("payments come back newest first")
    void returnsPaymentsNewestFirst() {
        PaymentRecord first = save("first");
        PaymentRecord second = save("second");

        List<PaymentRecord> payments = repository.findAllNewestFirst();

        assertEquals(List.of(second.id(), first.id()), payments.stream().map(PaymentRecord::id).toList());
    }

    @Test
    @DisplayName("saving keeps the record as it was given")
    void keepsTheRecordUnchanged() {
        PaymentRecord record = save("first");

        assertEquals(record, repository.findAllNewestFirst().get(0));
    }

    @Test
    @DisplayName("the returned list is a snapshot, not a live view")
    void returnsASnapshot() {
        save("first");
        List<PaymentRecord> snapshot = repository.findAllNewestFirst();

        save("second");

        assertEquals(1, snapshot.size());
        assertEquals(2, repository.findAllNewestFirst().size());
    }

    @Test
    @DisplayName("the repository is safe to use from several threads")
    void supportsConcurrentSaves() throws InterruptedException {
        Thread first = new Thread(() -> save("first"));
        Thread second = new Thread(() -> save("second"));
        first.start();
        second.start();
        first.join();
        second.join();

        assertEquals(2, repository.findAllNewestFirst().size());
    }

    private PaymentRecord save(String id) {
        PaymentRecord record =
                new PaymentRecord(id, "800197268-4", "Sneakers", PaymentStatus.APPROVED, java.time.Instant.now(), quote());
        repository.save(record);
        return record;
    }

    private Quote quote() {
        Payment payment = new BasePayment(2_000_000L, "Sneakers");
        return new Quote("COP", payment.baseAmountCop(), payment.payerTotalCop(), payment.merchantNetCop(),
                payment.layers());
    }
}
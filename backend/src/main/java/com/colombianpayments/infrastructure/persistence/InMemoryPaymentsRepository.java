package com.colombianpayments.infrastructure.persistence;

import com.colombianpayments.application.PaymentRecord;
import com.colombianpayments.application.PaymentsRepository;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

/** Keeps payments in memory for the length of the process, newest first. */
@Repository
public class InMemoryPaymentsRepository implements PaymentsRepository {

    private final ConcurrentLinkedDeque<PaymentRecord> payments = new ConcurrentLinkedDeque<>();

    @Override
    public void save(PaymentRecord payment) {
        payments.addFirst(payment);
    }

    @Override
    public List<PaymentRecord> findAllNewestFirst() {
        return List.copyOf(payments);
    }
}
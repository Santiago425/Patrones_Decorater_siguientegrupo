package com.colombianpayments.application;

import java.util.List;

/** Where created payments are kept. The demo ships an in memory implementation. */
public interface PaymentsRepository {

    void save(PaymentRecord payment);

    /** Every stored payment, newest first. */
    List<PaymentRecord> findAllNewestFirst();
}
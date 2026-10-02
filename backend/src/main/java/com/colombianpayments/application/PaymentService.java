package com.colombianpayments.application;

import com.colombianpayments.domain.BasePayment;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.PaymentRuleException;
import com.colombianpayments.domain.option.OptionCatalog;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Prices a payment and keeps the ones that are created. */
@Service
public class PaymentService {

    private static final String CURRENCY = "COP";

    private final OptionCatalog catalog;
    private final PaymentsRepository payments;
    private final Clock clock;

    public PaymentService(OptionCatalog catalog, PaymentsRepository payments, Clock clock) {
        this.catalog = catalog;
        this.payments = payments;
        this.clock = clock;
    }

    /** Prices a payment without storing anything. */
    public Quote quote(QuoteCommand command) {
        return price(command);
    }

    /** Prices a payment and keeps it as approved. */
    public PaymentRecord create(QuoteCommand command) {
        PaymentRecord payment = new PaymentRecord(
                UUID.randomUUID().toString(),
                command.merchantNit(),
                command.description(),
                PaymentStatus.APPROVED,
                clock.instant(),
                price(command));
        payments.save(payment);
        return payment;
    }

    public List<PaymentRecord> list() {
        return payments.findAllNewestFirst();
    }

    private Quote price(QuoteCommand command) {
        Payment payment;
        try {
            payment = catalog.decorate(new BasePayment(command.amountCop(), command.description()), command.optionCodes());
        } catch (PaymentRuleException e) {
            throw new InvalidPaymentException(e.getMessage());
        }
        return new Quote(
                CURRENCY,
                payment.baseAmountCop(),
                payment.payerTotalCop(),
                payment.merchantNetCop(),
                payment.layers());
    }
}
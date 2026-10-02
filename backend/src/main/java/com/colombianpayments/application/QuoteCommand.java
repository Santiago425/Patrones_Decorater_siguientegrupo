package com.colombianpayments.application;

import java.util.List;

/** What the client asks to be priced. */
public record QuoteCommand(long amountCop, String merchantNit, String description, List<String> optionCodes) {
}
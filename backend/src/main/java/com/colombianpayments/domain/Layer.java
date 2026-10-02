package com.colombianpayments.domain;

import java.util.List;

/** One charge or credit added by a decorator, as it is shown in the breakdown. */
public record Layer(LayerCode code, String label, long amountCop, Bearer bearer, List<String> notes) {
}
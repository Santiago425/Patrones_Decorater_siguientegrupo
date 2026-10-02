package com.colombianpayments.domain.option;

import com.colombianpayments.domain.LayerCode;
import com.colombianpayments.domain.Payment;
import com.colombianpayments.domain.PaymentRuleException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The five options in their fixed wrapping order. The order the client asks for them does not matter:
 * the catalog always wraps the selected options innermost first, which is what keeps GMF on top of the
 * VAT it was defined on.
 */
public final class OptionCatalog {

    private final List<PaymentOption> options;
    private final Map<String, PaymentOption> byCode;

    private OptionCatalog(List<PaymentOption> options) {
        this.options = List.copyOf(options);
        Map<String, PaymentOption> codes = new LinkedHashMap<>();
        for (PaymentOption option : this.options) {
            codes.put(option.code().name(), option);
        }
        this.byCode = Map.copyOf(codes);
    }

    /** Builds the catalog with the given UVT value, used by the withholding threshold. */
    public static OptionCatalog withUvt(long uvtCop) {
        return new OptionCatalog(List.of(
                new VatOption(),
                new GatewayFeeOption(),
                new WithholdingOption(uvtCop),
                new GmfOption(),
                new CashbackOption()));
    }

    /** The options as they are shown to the client, in wrapping order. */
    public List<PaymentOption> options() {
        return options;
    }

    /** Wraps {@code base} with the requested options, ignoring duplicates and unknown order. */
    public Payment decorate(Payment base, Collection<String> requestedCodes) {
        Set<LayerCode> selected = select(requestedCodes);
        Payment payment = base;
        for (PaymentOption option : options) {
            if (selected.contains(option.code())) {
                payment = option.apply(payment);
            }
        }
        return payment;
    }

    private Set<LayerCode> select(Collection<String> requestedCodes) {
        Set<LayerCode> selected = new LinkedHashSet<>();
        if (requestedCodes == null) {
            return selected;
        }
        for (String requested : requestedCodes) {
            String code = requested == null ? "" : requested.trim();
            PaymentOption option = byCode.get(code);
            if (option == null) {
                throw new PaymentRuleException("Unknown option code: " + code);
            }
            selected.add(option.code());
        }
        return selected;
    }
}
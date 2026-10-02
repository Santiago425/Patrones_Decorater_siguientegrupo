package com.colombianpayments.infrastructure.config;

import com.colombianpayments.domain.option.OptionCatalog;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the domain services that need configuration values. */
@Configuration
public class PaymentsConfiguration {

    /** The UVT is a demo value, so the withholding threshold follows {@code payments.uvt-cop}. */
    @Bean
    public OptionCatalog optionCatalog(@Value("${payments.uvt-cop:52374}") long uvtCop) {
        return OptionCatalog.withUvt(uvtCop);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
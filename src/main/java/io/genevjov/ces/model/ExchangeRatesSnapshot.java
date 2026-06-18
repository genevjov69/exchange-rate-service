package io.genevjov.ces.model;

import io.genevjov.ces.enums.ExchangeRateProviderName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

public record ExchangeRatesSnapshot(
        Currency base,
        Map<Currency, BigDecimal> rates,
        ExchangeRateProviderName provider,
        Instant timestamp) {
}

package io.genevjov.ces.dto.response;

import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

@Schema(description = "Latest exchange rates for one base currency")
public record BatchExchangeRatesResponse(
        @Schema(example = "EUR")
        Currency base,
        @Schema(example = "{\"USD\":1.08,\"GBP\":0.84,\"BGN\":1.9558}")
        Map<Currency, BigDecimal> rates,
        @Schema(example = "EXCHANGERATE_HOST")
        ExchangeRateProviderName provider,
        @Schema(example = "2026-06-17T10:00:00Z")
        Instant timestamp) {
}

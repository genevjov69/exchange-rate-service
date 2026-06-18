package io.genevjov.ces.dto.response;

import io.genevjov.ces.enums.ExchangeRateProvider;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

@Schema(description = "Latest exchange rates for one base currency")
public record AllExchangeRatesResponse(
        @Schema(example = "EUR")
        Currency base,
        @Schema(example = "{\"USD\":1.08,\"GBP\":0.84,\"BGN\":1.9558}")
        Map<Currency, BigDecimal> rates,
        @Schema(example = "EXCHANGERATE_HOST")
        ExchangeRateProvider provider,
        @Schema(example = "2026-06-17T10:00:00Z")
        Instant timestamp) {
}

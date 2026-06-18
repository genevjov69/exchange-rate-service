package io.genevjov.ces.dto.response;

import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

@Schema(description = "Exchange rate from one currency to another")
public record ExchangeRateResponse(
        @Schema(example = "EUR")
        Currency base,
        @Schema(example = "USD")
        Currency target,
        @Schema(example = "1.08")
        BigDecimal rate,
        @Schema(example = "EXCHANGERATE_HOST")
        ExchangeRateProviderName provider,
        @Schema(example = "2026-06-17T10:00:00Z")
        Instant timestamp) {
}

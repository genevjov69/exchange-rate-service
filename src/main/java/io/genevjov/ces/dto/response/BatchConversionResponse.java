package io.genevjov.ces.dto.response;

import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

@Schema(description = "Batch conversion response")
public record BatchConversionResponse(
        @Schema(example = "EUR")
        Currency from,
        @Schema(example = "100")
        BigDecimal amount,
        List<ConversionItemResponse> conversions,
        @Schema(example = "EXCHANGERATE_HOST")
        ExchangeRateProviderName provider,
        @Schema(example = "2026-06-17T10:00:00Z")
        Instant timestamp) {

    @Schema(description = "One converted target currency")
    public record ConversionItemResponse(
            @Schema(example = "USD")
            Currency to,
            @Schema(example = "1.08")
            BigDecimal rate,
            @Schema(example = "108.0000")
            BigDecimal convertedAmount) {
    }
}

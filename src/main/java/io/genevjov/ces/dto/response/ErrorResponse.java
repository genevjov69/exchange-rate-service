package io.genevjov.ces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API error response")
public record ErrorResponse(
        @Schema(example = "2026-06-17T10:00:00Z")
        Instant timestamp,
        @Schema(example = "400")
        int status,
        @Schema(example = "Bad Request")
        String error,
        @Schema(example = "Invalid currency code: EU")
        String message,
        @Schema(example = "/api/v1/exchange-rates/EU/USD")
        String path) {
}

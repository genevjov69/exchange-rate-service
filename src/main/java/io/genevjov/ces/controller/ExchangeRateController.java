package io.genevjov.ces.controller;

import io.genevjov.ces.dto.response.AllExchangeRatesResponse;
import io.genevjov.ces.dto.response.ErrorResponse;
import io.genevjov.ces.dto.response.ExchangeRateResponse;
import io.genevjov.ces.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;

@RestController
@RequestMapping("/api/v1/exchange-rates")
@Tag(name = "Exchange rates", description = "Exchange-rate lookup endpoints")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ExchangeRateController {

    ExchangeRateService exchangeRateService;

    @GetMapping("/{base}/{target}")
    @Operation(
            summary = "Get a pair exchange rate",
            description = "Returns the latest available exchange rate from the base currency to the target currency. Inputs are normalized to uppercase ISO-like 3-letter codes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exchange rate returned",
                    content = @Content(schema = @Schema(implementation = ExchangeRateResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "base": "EUR",
                                      "target": "USD",
                                      "rate": 1.08,
                                      "provider": "EXCHANGERATE_HOST",
                                      "asOf": "2026-06-17T10:00:00Z"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid currency code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Target currency/rate not available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "All external providers failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ExchangeRateResponse getExchangeRate(
            @Parameter(description = "Base currency code", example = "EUR")
            @PathVariable Currency base,
            @Parameter(description = "Target currency code", example = "USD")
            @PathVariable Currency target) {
        return exchangeRateService.getExchangeRate(base, target);
    }

    @GetMapping("/{base}")
    @Operation(
            summary = "Get all exchange rates for a base currency",
            description = "Fetches and caches the latest available rates for the supplied base currency. Cache key is the normalized base currency.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exchange rates returned",
                    content = @Content(schema = @Schema(implementation = AllExchangeRatesResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "base": "EUR",
                                      "rates": {
                                        "USD": 1.08,
                                        "GBP": 0.84,
                                        "BGN": 1.9558
                                      },
                                      "provider": "EXCHANGERATE_HOST",
                                      "asOf": "2026-06-17T10:00:00Z"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid currency code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "All external providers failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AllExchangeRatesResponse getAllExchangeRates(
            @Parameter(description = "Base currency code", example = "EUR")
            @PathVariable Currency base) {
        return exchangeRateService.getAllExchangeRates(base);
    }
}

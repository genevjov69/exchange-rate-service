package io.genevjov.ces.controller;

import io.genevjov.ces.dto.request.BatchConversionRequest;
import io.genevjov.ces.dto.response.BatchConversionResponse;
import io.genevjov.ces.dto.response.ConversionResponse;
import io.genevjov.ces.dto.response.ErrorResponse;
import io.genevjov.ces.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Currency;

@RestController
@RequestMapping("/api/v1/conversions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor

public class ConversionController {

    ConversionService conversionService;

    @GetMapping
    @Operation(
            summary = "Convert one amount to one target currency",
            description = "Converts a positive decimal amount from one currency to another using the latest rate snapshot for the base currency.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversion returned",
                    content = @Content(schema = @Schema(implementation = ConversionResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "from": "EUR",
                                      "to": "USD",
                                      "amount": 100,
                                      "rate": 1.08,
                                      "convertedAmount": 108.0000,
                                      "provider": "EXCHANGERATE_HOST",
                                      "asOf": "2026-06-17T10:00:00Z"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid currency or amount",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Target currency/rate not available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "All external providers failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ConversionResponse convert(
            @Parameter(description = "Source currency code", example = "EUR")
            @RequestParam Currency from,
            @Parameter(description = "Target currency code", example = "USD")
            @RequestParam Currency to,
            @Parameter(description = "Positive amount to convert", example = "100")
            @RequestParam BigDecimal amount) {
        return conversionService.convert(from, to, amount);
    }

    @PostMapping("/batch")
    @Operation(
            summary = "Convert one amount to multiple target currencies",
            description = "Converts a positive decimal amount from one currency to a de-duplicated ordered list of target currencies using one latest base snapshot.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch conversions returned",
                    content = @Content(schema = @Schema(implementation = BatchConversionResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "from": "EUR",
                                      "amount": 100,
                                      "conversions": [
                                        {
                                          "to": "USD",
                                          "rate": 1.08,
                                          "convertedAmount": 108.0000
                                        },
                                        {
                                          "to": "GBP",
                                          "rate": 0.84,
                                          "convertedAmount": 84.0000
                                        }
                                      ],
                                      "provider": "EXCHANGERATE_HOST",
                                      "asOf": "2026-06-17T10:00:00Z"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid currency, amount, or targets list",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "A target currency/rate is not available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "All external providers failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BatchConversionResponse batchConvert(@Valid @RequestBody BatchConversionRequest request) {
        return conversionService.batchConvert(request);
    }
}

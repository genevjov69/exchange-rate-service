package io.genevjov.ces.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Schema(description = "Batch currency conversion request")
public record BatchConversionRequest(@NotNull
                                     @Schema(example = "EUR")
                                     Currency from,
                                     @NotNull
                                     @Positive
                                     @Schema(example = "100")
                                     BigDecimal amount,
                                     @NotEmpty
                                     @ArraySchema(schema = @Schema(example = "USD"))
                                     List<@NotNull Currency> targets) {
}

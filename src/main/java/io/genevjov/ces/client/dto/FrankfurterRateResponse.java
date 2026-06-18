package io.genevjov.ces.client.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.genevjov.ces.utils.LenientCurrencyDeserializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public record FrankfurterRateResponse(LocalDate date,
                                      @JsonDeserialize(using = LenientCurrencyDeserializer.class)
                                      Currency base,
                                      @JsonDeserialize(using = LenientCurrencyDeserializer.class)
                                      Currency quote,
                                      BigDecimal rate) {
}

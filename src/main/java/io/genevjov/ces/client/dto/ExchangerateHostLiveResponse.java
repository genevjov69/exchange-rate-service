package io.genevjov.ces.client.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.genevjov.ces.utils.LenientCurrencyDeserializer;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

public record ExchangerateHostLiveResponse(Boolean success,
                                           @JsonDeserialize(using = LenientCurrencyDeserializer.class)
                                           Currency source,
                                           Long timestamp,
                                           Map<String, BigDecimal> quotes,
                                           ExchangerateHostErrorResponse error) {
}

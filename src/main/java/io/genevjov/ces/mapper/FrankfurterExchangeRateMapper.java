package io.genevjov.ces.mapper;

import io.genevjov.ces.client.dto.FrankfurterRateResponse;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface FrankfurterExchangeRateMapper {

    default ExchangeRatesSnapshot toSnapshot(
            Currency requestedBase,
            List<FrankfurterRateResponse> response,
            ExchangeRateProviderName provider) {
        return toSnapshot(
                baseCurrency(requestedBase, response),
                rates(requestedBase, response),
                provider,
                timestamp(response));
    }

    ExchangeRatesSnapshot toSnapshot(
            Currency base,
            Map<Currency, BigDecimal> rates,
            ExchangeRateProviderName provider,
            Instant timestamp);

    default Currency baseCurrency(Currency requestedBase, List<FrankfurterRateResponse> response) {
        return response.stream()
                .map(FrankfurterRateResponse::base)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(requestedBase);
    }

    default Map<Currency, BigDecimal> rates(Currency requestedBase, List<FrankfurterRateResponse> response) {
        Map<Currency, BigDecimal> rates = new LinkedHashMap<>();
        response.forEach(rateResponse -> {
            if (rateResponse.quote() != null && rateResponse.rate() != null) {
                rates.put(rateResponse.quote(), rateResponse.rate());
            }
        });

        return rates;
    }

    default Instant timestamp(List<FrankfurterRateResponse> response) {
        LocalDate latestDate = response.stream()
                .map(FrankfurterRateResponse::date)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        return latestDate == null ? Instant.now() : latestDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}

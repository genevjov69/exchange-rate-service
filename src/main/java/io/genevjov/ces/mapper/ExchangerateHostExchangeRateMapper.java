package io.genevjov.ces.mapper;

import io.genevjov.ces.client.dto.ExchangerateHostLiveResponse;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import io.genevjov.ces.utils.ExchangeRateProviderUtils;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ExchangerateHostExchangeRateMapper {

    default ExchangeRatesSnapshot toSnapshot(
            Currency requestedBase,
            ExchangerateHostLiveResponse response,
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

    default Currency baseCurrency(Currency requestedBase, ExchangerateHostLiveResponse response) {
        return response.source() == null ? requestedBase : response.source();
    }

    default Map<Currency, BigDecimal> rates(Currency requestedBase, ExchangerateHostLiveResponse response) {
        Currency source = baseCurrency(requestedBase, response);
        Map<Currency, BigDecimal> rates = new LinkedHashMap<>();

        response.quotes().forEach((quoteKey, rate) -> {
            Currency target = targetCurrency(source, quoteKey);
            if (target != null && rate != null) {
                rates.put(target, rate);
            }
        });
        return rates;
    }

    default Instant timestamp(ExchangerateHostLiveResponse response) {
        return response.timestamp() == null ? Instant.now() : Instant.ofEpochSecond(response.timestamp());
    }

    default Currency targetCurrency(Currency source, String quoteKey) {
        String sourceCode = source.getCurrencyCode();
        String targetCode = quoteKey.startsWith(sourceCode) ? quoteKey.substring(sourceCode.length()) : quoteKey;
        return ExchangeRateProviderUtils.currencyOrNull(targetCode);
    }
}

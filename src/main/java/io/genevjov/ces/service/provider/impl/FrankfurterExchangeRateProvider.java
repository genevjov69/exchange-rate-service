package io.genevjov.ces.service.provider.impl;

import io.genevjov.ces.client.FrankfurterFeignClient;
import io.genevjov.ces.client.dto.FrankfurterRateResponse;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.mapper.FrankfurterExchangeRateMapper;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import io.genevjov.ces.service.provider.ExchangeRateProvider;
import io.genevjov.ces.utils.ExchangeRateProviderUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Currency;
import java.util.List;

import static io.genevjov.ces.enums.ExchangeRateProviderName.FRANKFURTER;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FrankfurterExchangeRateProvider implements ExchangeRateProvider {

    private static final ExchangeRateProviderName PROVIDER = FRANKFURTER;


    FrankfurterFeignClient feignClient;
    FrankfurterExchangeRateMapper mapper;

    @Override
    public ExchangeRateProviderName getProvider() {
        return PROVIDER;
    }

    @Override
    public ExchangeRatesSnapshot getLatestRates(Currency base, Collection<Currency> targets) {
        try {
            List<FrankfurterRateResponse> response = CollectionUtils.isEmpty(targets)
                    ? feignClient.latestRates(base)
                    : feignClient.latestRates(
                    base, ExchangeRateProviderUtils.commaSeparatedCurrencyCodes(targets));

            validateResponse(base, response);
            ExchangeRatesSnapshot snapshot = mapper.toSnapshot(base, response, PROVIDER);
            validateSnapshot(base, snapshot);

            return snapshot;
        } catch (RuntimeException ex) {
            throw ExchangeRateProviderUtils.providerException(PROVIDER, ex);
        }
    }

    private void validateResponse(Currency base, List<FrankfurterRateResponse> response) {
        if (CollectionUtils.isEmpty(response)) {
            throw new CurrencyNotFoundException("No exchange rates returned for " + base.getCurrencyCode());
        }
    }

    private void validateSnapshot(Currency base, ExchangeRatesSnapshot snapshot) {
        if (snapshot == null || CollectionUtils.isEmpty(snapshot.rates())) {
            throw new CurrencyNotFoundException("No exchange rates returned for " + base.getCurrencyCode());
        }
    }
}

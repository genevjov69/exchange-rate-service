package io.genevjov.ces.service.provider;

import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.model.ExchangeRatesSnapshot;

import java.util.Collection;
import java.util.Currency;

public interface ExchangeRateProvider {

    ExchangeRateProviderName getProvider();

    ExchangeRatesSnapshot getLatestRates(Currency base, Collection<Currency> targets);
}

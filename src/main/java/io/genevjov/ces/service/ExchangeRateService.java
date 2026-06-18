package io.genevjov.ces.service;

import io.genevjov.ces.dto.response.BatchExchangeRatesResponse;
import io.genevjov.ces.dto.response.ExchangeRateResponse;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.mapper.ExchangeRateResponseMapper;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ExchangeRateService {

    ExchangeRateProviderRouter exchangeRateProviderRouter;
    ExchangeRateResponseMapper exchangeRateResponseMapper;

    public ExchangeRateResponse getExchangeRate(Currency base, Currency target) {
        ExchangeRatesSnapshot snapshot = getExchangeRates(base, List.of(target));
        BigDecimal rate = snapshot.rates().get(target);
        if (rate == null) {
            throw new CurrencyNotFoundException("No exchange rate available for "
                    + base.getCurrencyCode() + " -> " + target.getCurrencyCode());
        }

        return exchangeRateResponseMapper.toExchangeRateResponse(snapshot, target, rate);
    }

    public BatchExchangeRatesResponse getAllExchangeRates(Currency base) {
        ExchangeRatesSnapshot snapshot = getExchangeRates(base, List.of());
        return exchangeRateResponseMapper.toBatchExchangeRatesResponse(snapshot);
    }

    ExchangeRatesSnapshot getExchangeRates(Currency base, Collection<Currency> targets) {
        return exchangeRateProviderRouter.getLatestRates(base, targets);
    }
}

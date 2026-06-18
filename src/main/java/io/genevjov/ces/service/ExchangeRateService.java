package io.genevjov.ces.service;

import io.genevjov.ces.data.ExchangeRateCacheKey;
import io.genevjov.ces.data.ExchangeRateSnapshotStore;
import io.genevjov.ces.dto.response.BatchExchangeRatesResponse;
import io.genevjov.ces.dto.response.ExchangeRateResponse;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.mapper.ExchangeRateResponseMapper;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    ExchangeRateProviderRouter exchangeRateProviderRouter;
    ExchangeRateSnapshotStore exchangeRateSnapshotStore;
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
        ExchangeRatesSnapshot snapshot = getSnapshot(base);
        if (targets != null && targets.contains(base)) {
            Map<Currency, BigDecimal> rates = new LinkedHashMap<>(snapshot.rates());
            rates.put(base, BigDecimal.ONE);
            return new ExchangeRatesSnapshot(snapshot.base(), rates, snapshot.provider(), snapshot.timestamp());
        }

        return snapshot;
    }

    private ExchangeRatesSnapshot getSnapshot(Currency base) {
        ExchangeRateCacheKey key = ExchangeRateCacheKey.from(base);
        return exchangeRateSnapshotStore.get(key, missingKey -> {
            log.info("Exchange rate snapshot cache miss for base={}", missingKey.base());
            return exchangeRateProviderRouter.getLatestRates(missingKey.base(), List.of());
        });
    }
}

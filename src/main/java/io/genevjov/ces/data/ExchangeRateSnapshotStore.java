package io.genevjov.ces.data;

import io.genevjov.ces.model.ExchangeRatesSnapshot;

import java.util.function.Function;

public interface ExchangeRateSnapshotStore {

    ExchangeRatesSnapshot get(ExchangeRateCacheKey key, Function<ExchangeRateCacheKey, ExchangeRatesSnapshot> loader);
}

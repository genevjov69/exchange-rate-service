package io.genevjov.ces.data;

import io.genevjov.ces.model.ExchangeRatesSnapshot;

import java.util.Optional;

public interface ExchangeRateSnapshotStore {

    Optional<ExchangeRatesSnapshot> findByKey(ExchangeRateCacheKey key);

    void save(ExchangeRateCacheKey key, ExchangeRatesSnapshot snapshot);
}

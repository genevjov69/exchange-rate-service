package io.genevjov.ces.data.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.genevjov.ces.configuration.CacheProperties;
import io.genevjov.ces.data.ExchangeRateCacheKey;
import io.genevjov.ces.data.ExchangeRateSnapshotStore;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CaffeineExchangeRateSnapshotStore implements ExchangeRateSnapshotStore {

    private final Cache<ExchangeRateCacheKey, ExchangeRatesSnapshot> cache;

    public CaffeineExchangeRateSnapshotStore(CacheProperties cacheProperties) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(cacheProperties.getTtl())
                .build();
    }

    @Override
    public Optional<ExchangeRatesSnapshot> findByKey(ExchangeRateCacheKey key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public void save(ExchangeRateCacheKey key, ExchangeRatesSnapshot snapshot) {
        cache.put(key, snapshot);
    }
}

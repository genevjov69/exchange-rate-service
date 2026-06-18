package io.genevjov.ces.data.cache;

import io.genevjov.ces.configuration.CacheProperties;
import io.genevjov.ces.data.ExchangeRateCacheKey;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineExchangeRateSnapshotStoreTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void expiresEntriesAfterConfiguredTtl() throws InterruptedException {
        CacheProperties cacheProperties = new CacheProperties();
        cacheProperties.setTtl(Duration.ofMillis(50));
        CaffeineExchangeRateSnapshotStore store = new CaffeineExchangeRateSnapshotStore(cacheProperties);
        ExchangeRateCacheKey key = ExchangeRateCacheKey.from(EUR);
        ExchangeRatesSnapshot snapshot = new ExchangeRatesSnapshot(
                EUR,
                Map.of(USD, BigDecimal.valueOf(1.1)),
                ExchangeRateProviderName.FRANKFURTER,
                Instant.parse("2026-06-18T00:00:00Z"));

        store.save(key, snapshot);

        assertThat(store.findByKey(key)).contains(snapshot);
        Thread.sleep(80);
        assertThat(store.findByKey(key)).isEmpty();
    }
}

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

        store.get(key, ignored -> snapshot);

        assertThat(store.get(key, ignored -> snapshot)).isSameAs(snapshot);
        Thread.sleep(80);
        ExchangeRatesSnapshot refreshedSnapshot = new ExchangeRatesSnapshot(
                EUR,
                Map.of(USD, BigDecimal.valueOf(1.2)),
                ExchangeRateProviderName.FRANKFURTER,
                Instant.parse("2026-06-18T00:01:00Z"));
        assertThat(store.get(key, ignored -> refreshedSnapshot)).isSameAs(refreshedSnapshot);
    }

    @Test
    void loadsSnapshotAtomicallyForConcurrentRequestsWithSameKey() throws InterruptedException {
        CacheProperties cacheProperties = new CacheProperties();
        cacheProperties.setTtl(Duration.ofMinutes(1));
        CaffeineExchangeRateSnapshotStore store = new CaffeineExchangeRateSnapshotStore(cacheProperties);
        ExchangeRateCacheKey key = ExchangeRateCacheKey.from(EUR);
        ExchangeRatesSnapshot snapshot = new ExchangeRatesSnapshot(
                EUR,
                Map.of(USD, BigDecimal.valueOf(1.1)),
                ExchangeRateProviderName.FRANKFURTER,
                Instant.parse("2026-06-18T00:00:00Z"));
        AtomicInteger loaderCalls = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(10);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    await(start);
                    return store.get(key, ignored -> {
                        loaderCalls.incrementAndGet();
                        sleep();
                        return snapshot;
                    });
                });
            }

            assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(loaderCalls).hasValue(1);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private static void sleep() {
        try {
            Thread.sleep((long) 50);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }
}

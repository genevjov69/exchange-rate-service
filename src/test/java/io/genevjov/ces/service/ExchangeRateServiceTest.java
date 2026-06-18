package io.genevjov.ces.service;

import io.genevjov.ces.data.ExchangeRateCacheKey;
import io.genevjov.ces.data.ExchangeRateSnapshotStore;
import io.genevjov.ces.dto.response.BatchExchangeRatesResponse;
import io.genevjov.ces.dto.response.ExchangeRateResponse;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.mapper.ExchangeRateResponseMapper;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.genevjov.ces.enums.ExchangeRateProviderName.FRANKFURTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ExchangeRateServiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Instant TIMESTAMP = Instant.parse("2026-06-18T00:00:00Z");

    @Test
    void cachesExchangeRateSnapshotByBaseOnly() {
        ExchangeRatesSnapshot snapshot = snapshot(Map.of(USD, BigDecimal.valueOf(1.1), GBP, BigDecimal.valueOf(0.85)));
        ExchangeRateProviderRouter router = mock(ExchangeRateProviderRouter.class);
        InMemoryExchangeRateSnapshotStore store = new InMemoryExchangeRateSnapshotStore();
        ExchangeRateService service = new ExchangeRateService(
                router,
                store,
                Mappers.getMapper(ExchangeRateResponseMapper.class));

        when(router.getLatestRates(eq(EUR), eq(List.of()))).thenReturn(snapshot);

        BatchExchangeRatesResponse allRates = service.getAllExchangeRates(EUR);
        ExchangeRateResponse pairRate = service.getExchangeRate(EUR, USD);
        ExchangeRatesSnapshot batchSnapshot = service.getExchangeRates(EUR, List.of(GBP, USD, USD));

        assertThat(allRates.rates()).containsEntry(USD, BigDecimal.valueOf(1.1));
        assertThat(pairRate.rate()).isEqualByComparingTo("1.1");
        assertThat(batchSnapshot).isSameAs(snapshot);
        verify(router).getLatestRates(EUR, List.of());
        verifyNoMoreInteractions(router);
    }

    @Test
    void addsBaseRateWithoutCreatingSeparateCacheEntry() {
        ExchangeRatesSnapshot snapshot = snapshot(Map.of(USD, BigDecimal.valueOf(1.1)));
        ExchangeRateProviderRouter router = mock(ExchangeRateProviderRouter.class);
        InMemoryExchangeRateSnapshotStore store = new InMemoryExchangeRateSnapshotStore();
        ExchangeRateService service = new ExchangeRateService(
                router,
                store,
                Mappers.getMapper(ExchangeRateResponseMapper.class));

        when(router.getLatestRates(EUR, List.of())).thenReturn(snapshot);

        ExchangeRatesSnapshot response = service.getExchangeRates(EUR, List.of(EUR, USD));

        assertThat(response.base()).isEqualTo(EUR);
        assertThat(response.rates()).containsEntry(EUR, BigDecimal.ONE);
        assertThat(response.rates()).containsEntry(USD, BigDecimal.valueOf(1.1));
        assertThat(response.provider()).isEqualTo(FRANKFURTER);
        assertThat(response.timestamp()).isEqualTo(TIMESTAMP);
        verify(router).getLatestRates(EUR, List.of());
        verifyNoMoreInteractions(router);
    }

    private static ExchangeRatesSnapshot snapshot(Map<Currency, BigDecimal> rates) {
        return new ExchangeRatesSnapshot(EUR, rates, ExchangeRateProviderName.FRANKFURTER, TIMESTAMP);
    }

    private static class InMemoryExchangeRateSnapshotStore implements ExchangeRateSnapshotStore {

        private final Map<ExchangeRateCacheKey, ExchangeRatesSnapshot> cache = new HashMap<>();

        @Override
        public ExchangeRatesSnapshot get(ExchangeRateCacheKey key, Function<ExchangeRateCacheKey, ExchangeRatesSnapshot> loader) {
            return cache.computeIfAbsent(key, loader);
        }
    }
}

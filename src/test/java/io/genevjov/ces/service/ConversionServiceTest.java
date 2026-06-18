package io.genevjov.ces.service;

import io.genevjov.ces.configuration.ConversionProperties;
import io.genevjov.ces.data.ExchangeRateCacheKey;
import io.genevjov.ces.data.ExchangeRateSnapshotStore;
import io.genevjov.ces.dto.request.BatchConversionRequest;
import io.genevjov.ces.dto.response.BatchConversionResponse;
import io.genevjov.ces.dto.response.ConversionResponse;
import io.genevjov.ces.mapper.ConversionResponseMapper;
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

class ConversionServiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Instant TIMESTAMP = Instant.parse("2026-06-18T00:00:00Z");

    @Test
    void conversionsUseSharedExchangeRateSnapshotCacheByBaseCurrency() {
        ExchangeRateProviderRouter router = mock(ExchangeRateProviderRouter.class);
        ExchangeRateService exchangeRateService = new ExchangeRateService(
                router,
                new InMemoryExchangeRateSnapshotStore(),
                Mappers.getMapper(ExchangeRateResponseMapper.class));
        ConversionService conversionService = conversionService(exchangeRateService);
        ExchangeRatesSnapshot snapshot = snapshot(Map.of(
                USD, BigDecimal.valueOf(1.1565),
                GBP, BigDecimal.valueOf(0.86515)));
        when(router.getLatestRates(eq(EUR), eq(List.of()))).thenReturn(snapshot);
        BatchConversionRequest request = new BatchConversionRequest(
                EUR,
                BigDecimal.valueOf(100),
                List.of(USD, GBP, USD));

        ConversionResponse single = conversionService.convert(EUR, USD, BigDecimal.valueOf(100));
        BatchConversionResponse batch = conversionService.batchConvert(request);

        assertThat(single.convertedAmount()).isEqualByComparingTo("115.6500");
        assertThat(single.provider()).isEqualTo(FRANKFURTER);
        assertThat(batch.conversions()).extracting(BatchConversionResponse.ConversionItemResponse::to)
                .containsExactly(USD, GBP);
        assertThat(batch.conversions()).extracting(BatchConversionResponse.ConversionItemResponse::convertedAmount)
                .containsExactly(new BigDecimal("115.6500"), new BigDecimal("86.5150"));
        verify(router).getLatestRates(EUR, List.of());
        verifyNoMoreInteractions(router);
    }

    private static ConversionService conversionService(ExchangeRateService exchangeRateService) {
        ConversionProperties conversionProperties = new ConversionProperties();
        conversionProperties.setConversionScale(4);
        return new ConversionService(
                conversionProperties,
                exchangeRateService,
                Mappers.getMapper(ConversionResponseMapper.class));
    }

    private static ExchangeRatesSnapshot snapshot(Map<Currency, BigDecimal> rates) {
        return new ExchangeRatesSnapshot(EUR, rates, FRANKFURTER, TIMESTAMP);
    }

    private static class InMemoryExchangeRateSnapshotStore implements ExchangeRateSnapshotStore {

        private final Map<ExchangeRateCacheKey, ExchangeRatesSnapshot> cache = new HashMap<>();

        @Override
        public ExchangeRatesSnapshot get(ExchangeRateCacheKey key, Function<ExchangeRateCacheKey, ExchangeRatesSnapshot> loader) {
            return cache.computeIfAbsent(key, loader);
        }
    }
}

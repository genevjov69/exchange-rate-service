package io.genevjov.ces.data;

import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class CacheKeyTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void exchangeRateKeyUsesOnlyBaseCurrency() {
        ExchangeRateCacheKey first = ExchangeRateCacheKey.from(EUR);
        ExchangeRateCacheKey second = ExchangeRateCacheKey.from(EUR);

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(ExchangeRateCacheKey.from(USD));
    }
}

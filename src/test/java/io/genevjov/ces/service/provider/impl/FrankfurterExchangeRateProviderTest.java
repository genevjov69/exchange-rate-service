package io.genevjov.ces.service.provider.impl;

import io.genevjov.ces.client.FrankfurterFeignClient;
import io.genevjov.ces.client.dto.FrankfurterRateResponse;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.mapper.FrankfurterExchangeRateMapper;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static io.genevjov.ces.enums.ExchangeRateProviderName.FRANKFURTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FrankfurterExchangeRateProviderTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void mapsSupportedRatesAndSkipsUnsupportedCurrencies() {
        FrankfurterFeignClient feignClient = mock(FrankfurterFeignClient.class);
        FrankfurterExchangeRateProvider provider = new FrankfurterExchangeRateProvider(
                feignClient,
                Mappers.getMapper(FrankfurterExchangeRateMapper.class));
        when(feignClient.latestRates(EUR)).thenReturn(List.of(
                new FrankfurterRateResponse(LocalDate.parse("2026-06-18"), EUR, USD, BigDecimal.valueOf(1.1)),
                new FrankfurterRateResponse(LocalDate.parse("2026-06-18"), EUR, null, BigDecimal.valueOf(1.2))));

        ExchangeRatesSnapshot snapshot = provider.getLatestRates(EUR, List.of());

        assertThat(snapshot.base()).isEqualTo(EUR);
        assertThat(snapshot.provider()).isEqualTo(FRANKFURTER);
        assertThat(snapshot.rates()).containsOnlyKeys(USD);
        assertThat(snapshot.rates().get(USD)).isEqualByComparingTo("1.1");
    }

    @Test
    void rejectsResponseWhenEveryRateIsFilteredOut() {
        FrankfurterFeignClient feignClient = mock(FrankfurterFeignClient.class);
        FrankfurterExchangeRateProvider provider = new FrankfurterExchangeRateProvider(
                feignClient,
                Mappers.getMapper(FrankfurterExchangeRateMapper.class));
        when(feignClient.latestRates(EUR)).thenReturn(List.of(
                new FrankfurterRateResponse(LocalDate.parse("2026-06-18"), EUR, null, BigDecimal.valueOf(1.2))));

        assertThatThrownBy(() -> provider.getLatestRates(EUR, List.of()))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("No exchange rates returned for EUR");
    }
}

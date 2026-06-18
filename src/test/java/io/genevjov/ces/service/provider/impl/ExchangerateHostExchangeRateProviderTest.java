package io.genevjov.ces.service.provider.impl;

import io.genevjov.ces.client.ExchangerateHostFeignClient;
import io.genevjov.ces.client.dto.ExchangerateHostErrorResponse;
import io.genevjov.ces.client.dto.ExchangerateHostLiveResponse;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.exception.ExternalProviderException;
import io.genevjov.ces.mapper.ExchangerateHostExchangeRateMapper;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.genevjov.ces.enums.ExchangeRateProviderName.EXCHANGERATE_HOST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExchangerateHostExchangeRateProviderTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void mapsSupportedRatesAndSkipsUnsupportedCurrencies() {
        ExchangerateHostFeignClient feignClient = mock(ExchangerateHostFeignClient.class);
        ExchangerateHostExchangeRateProvider provider = provider(feignClient);
        when(feignClient.latestRates(EUR)).thenReturn(new ExchangerateHostLiveResponse(
                true,
                EUR,
                1_803_000_000L,
                quotes("EURUSD", BigDecimal.valueOf(1.1), "EURCNH", BigDecimal.valueOf(1.2)),
                null));

        ExchangeRatesSnapshot snapshot = provider.getLatestRates(EUR, List.of());

        assertThat(snapshot.base()).isEqualTo(EUR);
        assertThat(snapshot.provider()).isEqualTo(EXCHANGERATE_HOST);
        assertThat(snapshot.timestamp()).isEqualTo(Instant.ofEpochSecond(1_803_000_000L));
        assertThat(snapshot.rates()).containsOnlyKeys(USD);
        assertThat(snapshot.rates().get(USD)).isEqualByComparingTo("1.1");
    }

    @Test
    void callsTargetedClientEndpointWhenTargetsAreProvided() {
        ExchangerateHostFeignClient feignClient = mock(ExchangerateHostFeignClient.class);
        ExchangerateHostExchangeRateProvider provider = provider(feignClient);
        when(feignClient.latestRates(EUR, "USD,GBP")).thenReturn(new ExchangerateHostLiveResponse(
                true,
                EUR,
                null,
                quotes("EURUSD", BigDecimal.valueOf(1.1), "EURGBP", BigDecimal.valueOf(0.85)),
                null));

        ExchangeRatesSnapshot snapshot = provider.getLatestRates(EUR, List.of(USD, GBP));

        assertThat(snapshot.rates()).containsEntry(USD, BigDecimal.valueOf(1.1));
        assertThat(snapshot.rates()).containsEntry(GBP, BigDecimal.valueOf(0.85));
        verify(feignClient).latestRates(EUR, "USD,GBP");
    }

    @Test
    void rejectsEmptyProviderResponse() {
        ExchangerateHostFeignClient feignClient = mock(ExchangerateHostFeignClient.class);
        ExchangerateHostExchangeRateProvider provider = provider(feignClient);
        when(feignClient.latestRates(EUR)).thenReturn(null);

        assertThatThrownBy(() -> provider.getLatestRates(EUR, List.of()))
                .isInstanceOf(ExternalProviderException.class)
                .hasMessageContaining("Provider returned an empty response");
    }

    @Test
    void rejectsProviderErrorResponseUsingProviderInfoMessage() {
        ExchangerateHostFeignClient feignClient = mock(ExchangerateHostFeignClient.class);
        ExchangerateHostExchangeRateProvider provider = provider(feignClient);
        when(feignClient.latestRates(EUR)).thenReturn(new ExchangerateHostLiveResponse(
                false,
                EUR,
                null,
                null,
                new ExchangerateHostErrorResponse(101, "invalid_access_key", "Invalid access key")));

        assertThatThrownBy(() -> provider.getLatestRates(EUR, List.of()))
                .isInstanceOf(ExternalProviderException.class)
                .hasMessageContaining("Provider rejected the request: Invalid access key");
    }

    @Test
    void rejectsResponseWithoutQuotes() {
        ExchangerateHostFeignClient feignClient = mock(ExchangerateHostFeignClient.class);
        ExchangerateHostExchangeRateProvider provider = provider(feignClient);
        when(feignClient.latestRates(EUR)).thenReturn(new ExchangerateHostLiveResponse(
                true,
                EUR,
                null,
                Map.of(),
                null));

        assertThatThrownBy(() -> provider.getLatestRates(EUR, List.of()))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("No exchange rates returned for EUR");
    }

    @Test
    void rejectsResponseWhenEveryQuoteIsFilteredOutByMapper() {
        ExchangerateHostFeignClient feignClient = mock(ExchangerateHostFeignClient.class);
        ExchangerateHostExchangeRateProvider provider = provider(feignClient);
        when(feignClient.latestRates(EUR)).thenReturn(new ExchangerateHostLiveResponse(
                true,
                EUR,
                null,
                quotes("EURCNH", BigDecimal.valueOf(1.2)),
                null));

        assertThatThrownBy(() -> provider.getLatestRates(EUR, List.of()))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("No exchange rates returned for EUR");
    }

    private static ExchangerateHostExchangeRateProvider provider(ExchangerateHostFeignClient feignClient) {
        return new ExchangerateHostExchangeRateProvider(
                feignClient,
                Mappers.getMapper(ExchangerateHostExchangeRateMapper.class));
    }

    private static Map<String, BigDecimal> quotes(String firstCode, BigDecimal firstRate, Object... rest) {
        Map<String, BigDecimal> quotes = new LinkedHashMap<>();
        quotes.put(firstCode, firstRate);
        for (int index = 0; index < rest.length; index += 2) {
            quotes.put((String) rest[index], (BigDecimal) rest[index + 1]);
        }
        return quotes;
    }
}

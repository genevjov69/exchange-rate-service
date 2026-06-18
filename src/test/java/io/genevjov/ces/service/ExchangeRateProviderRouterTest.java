package io.genevjov.ces.service;

import io.genevjov.ces.configuration.ExchangeRateProperties;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.exception.ExternalProviderException;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import io.genevjov.ces.service.provider.ExchangeRateProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Currency;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static io.genevjov.ces.enums.ExchangeRateProviderName.EXCHANGERATE_HOST;
import static io.genevjov.ces.enums.ExchangeRateProviderName.FRANKFURTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateProviderRouterTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void fallsBackToLowerPriorityProviderWhenHigherPriorityFails() {
        ExchangeRatesSnapshot frankfurterSnapshot = snapshot(FRANKFURTER, Map.of(USD, BigDecimal.valueOf(1.1)));
        TestProvider exchangerateHost = new TestProvider(EXCHANGERATE_HOST, new ExternalProviderException("unauthorized"));
        TestProvider frankfurter = new TestProvider(FRANKFURTER, frankfurterSnapshot);

        ExchangeRateProviderRouter router = new ExchangeRateProviderRouter(
                providers(exchangerateHost, frankfurter),
                properties(EXCHANGERATE_HOST, FRANKFURTER));

        ExchangeRatesSnapshot snapshot = router.getLatestRates(EUR, List.of(USD));

        assertThat(snapshot).isSameAs(frankfurterSnapshot);
        assertThat(exchangerateHost.calls).isEqualTo(1);
        assertThat(frankfurter.calls).isEqualTo(1);
    }

    @Test
    void returnsBaseRateWithoutCallingProvidersWhenOnlyTargetIsBase() {
        TestProvider exchangerateHost = new TestProvider(EXCHANGERATE_HOST, snapshot(EXCHANGERATE_HOST, Map.of()));
        ExchangeRateProviderRouter router = new ExchangeRateProviderRouter(
                providers(exchangerateHost),
                properties(EXCHANGERATE_HOST));

        ExchangeRatesSnapshot snapshot = router.getLatestRates(EUR, List.of(EUR));

        assertThat(snapshot.base()).isEqualTo(EUR);
        assertThat(snapshot.rates()).containsEntry(EUR, BigDecimal.ONE);
        assertThat(snapshot.provider()).isEqualTo(EXCHANGERATE_HOST);
        assertThat(exchangerateHost.calls).isZero();
    }

    @Test
    void throwsExternalProviderExceptionWhenEveryProviderFails() {
        TestProvider exchangerateHost = new TestProvider(EXCHANGERATE_HOST, new ExternalProviderException("unauthorized"));
        TestProvider frankfurter = new TestProvider(FRANKFURTER, new ExternalProviderException("timeout"));
        ExchangeRateProviderRouter router = new ExchangeRateProviderRouter(
                providers(exchangerateHost, frankfurter),
                properties(EXCHANGERATE_HOST, FRANKFURTER));

        assertThatThrownBy(() -> router.getLatestRates(EUR, List.of(USD)))
                .isInstanceOf(ExternalProviderException.class)
                .hasMessageContaining("All exchange rate providers failed")
                .hasMessageContaining("EXCHANGERATE_HOST")
                .hasMessageContaining("FRANKFURTER");
    }

    private static ExchangeRateProperties properties(ExchangeRateProviderName... priority) {
        ExchangeRateProperties properties = new ExchangeRateProperties();
        properties.setProviderPriority(List.of(priority));
        return properties;
    }

    private static Map<ExchangeRateProviderName, ExchangeRateProvider> providers(TestProvider... providers) {
        Map<ExchangeRateProviderName, ExchangeRateProvider> result = new EnumMap<>(ExchangeRateProviderName.class);
        for (TestProvider provider : providers) {
            result.put(provider.getProvider(), provider);
        }
        return result;
    }

    private static ExchangeRatesSnapshot snapshot(ExchangeRateProviderName provider, Map<Currency, BigDecimal> rates) {
        return new ExchangeRatesSnapshot(EUR, rates, provider, Instant.parse("2026-06-18T00:00:00Z"));
    }

    private static class TestProvider implements ExchangeRateProvider {

        private final ExchangeRateProviderName provider;
        private final ExchangeRatesSnapshot snapshot;
        private final RuntimeException failure;
        private int calls;

        private TestProvider(ExchangeRateProviderName provider, ExchangeRatesSnapshot snapshot) {
            this.provider = provider;
            this.snapshot = snapshot;
            this.failure = null;
        }

        private TestProvider(ExchangeRateProviderName provider, RuntimeException failure) {
            this.provider = provider;
            this.snapshot = null;
            this.failure = failure;
        }

        @Override
        public ExchangeRateProviderName getProvider() {
            return provider;
        }

        @Override
        public ExchangeRatesSnapshot getLatestRates(Currency base, Collection<Currency> targets) {
            calls++;
            if (failure != null) {
                throw failure;
            }
            return snapshot;
        }
    }
}

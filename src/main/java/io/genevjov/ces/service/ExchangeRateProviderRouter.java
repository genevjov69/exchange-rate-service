package io.genevjov.ces.service;

import io.genevjov.ces.configuration.ExchangeRateProperties;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.exception.ExternalProviderException;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import io.genevjov.ces.service.provider.ExchangeRateProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateProviderRouter {

    Map<ExchangeRateProviderName, ExchangeRateProvider> providersByType;
    ExchangeRateProperties exchangeRateProperties;

    public ExchangeRatesSnapshot getLatestRates(Currency base, Collection<Currency> targets) {
        List<Currency> distinctTargets = distinctTargets(targets);
        if (!distinctTargets.isEmpty() && distinctTargets.stream().allMatch(base::equals)) {
            return new ExchangeRatesSnapshot(
                    base,
                    Map.of(base, BigDecimal.ONE),
                    firstConfiguredProvider(),
                    Instant.now());
        }

        List<Currency> externalTargets = distinctTargets.stream()
                .filter(target -> !base.equals(target))
                .toList();

        ExchangeRatesSnapshot snapshot = getRatesWithFallback(base, externalTargets);
        if (distinctTargets.contains(base)) {
            Map<Currency, BigDecimal> rates = new LinkedHashMap<>(snapshot.rates());
            rates.put(base, BigDecimal.ONE);
            return new ExchangeRatesSnapshot(snapshot.base(), rates, snapshot.provider(), snapshot.timestamp());
        }

        return snapshot;
    }

    private ExchangeRatesSnapshot getRatesWithFallback(Currency base, Collection<Currency> targets) {
        List<String> failures = new ArrayList<>();
        CurrencyNotFoundException notFoundException = null;
        List<ExchangeRateProviderName> priority = providerPriority();

        for (int index = 0; index < priority.size(); index++) {
            ExchangeRateProviderName provider = priority.get(index);
            ExchangeRateProvider providerStrategy = providersByType.get(provider);
            if (providerStrategy == null) {
                failures.add(provider + " has no configured strategy");
                logFallback(provider, nextProvider(priority, index), "provider strategy is not configured");
                continue;
            }

            try {
                ExchangeRatesSnapshot snapshot = providerStrategy.getLatestRates(base, targets);
                ensureTargetsAvailable(snapshot, targets);
                return snapshot;
            } catch (CurrencyNotFoundException ex) {
                notFoundException = ex;
                failures.add(provider + ": " + ex.getMessage());
                logFallback(provider, nextProvider(priority, index), ex.getMessage());
            } catch (ExternalProviderException ex) {
                failures.add(provider + ": " + ex.getMessage());
                logFallback(provider, nextProvider(priority, index), ex.getMessage());
            } catch (RuntimeException ex) {
                failures.add(provider + ": unexpected provider failure: " + ex.getMessage());
                logFallback(provider, nextProvider(priority, index), ex.getMessage());
            }
        }

        if (notFoundException != null) {
            throw notFoundException;
        }

        throw new ExternalProviderException("All exchange rate providers failed: " + String.join("; ", failures));
    }

    private void logFallback(ExchangeRateProviderName failedProvider, ExchangeRateProviderName nextProvider, String reason) {
        if (nextProvider == null) {
            log.warn("Exchange rate provider {} failed and no lower-priority provider remains: {}", failedProvider, reason);
            return;
        }

        log.warn("Exchange rate provider {} failed, falling back to {}: {}", failedProvider, nextProvider, reason);
    }

    private ExchangeRateProviderName nextProvider(List<ExchangeRateProviderName> priority, int currentIndex) {
        for (int index = currentIndex + 1; index < priority.size(); index++) {
            ExchangeRateProviderName provider = priority.get(index);
            if (providersByType.containsKey(provider)) {
                return provider;
            }
        }

        return null;
    }

    private void ensureTargetsAvailable(ExchangeRatesSnapshot snapshot, Collection<Currency> targets) {
        if (CollectionUtils.isEmpty(targets)) {
            return;
        }

        List<Currency> missingTargets = targets.stream()
                .filter(target -> !snapshot.rates().containsKey(target))
                .toList();

        if (!missingTargets.isEmpty()) {
            throw new CurrencyNotFoundException("No exchange rate available for "
                    + snapshot.base().getCurrencyCode() + " -> "
                    + missingTargets.getFirst().getCurrencyCode());
        }
    }

    private List<ExchangeRateProviderName> providerPriority() {
        List<ExchangeRateProviderName> configuredPriority = exchangeRateProperties.getProviderPriority();
        if (configuredPriority == null || configuredPriority.isEmpty()) {
            return new ArrayList<>(providersByType.keySet());
        }

        List<ExchangeRateProviderName> priority = configuredPriority.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return priority.isEmpty() ? new ArrayList<>(providersByType.keySet()) : priority;
    }

    private ExchangeRateProviderName firstConfiguredProvider() {
        return providerPriority().stream()
                .filter(providersByType::containsKey)
                .findFirst()
                .orElse(ExchangeRateProviderName.EXCHANGERATE_HOST);
    }

    private List<Currency> distinctTargets(Collection<Currency> targets) {
        if (CollectionUtils.isEmpty(targets)) {
            return List.of();
        }

        return targets.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}

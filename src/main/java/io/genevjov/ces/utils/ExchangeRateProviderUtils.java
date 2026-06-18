package io.genevjov.ces.utils;

import feign.FeignException;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.exception.ExternalProviderException;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Currency;
import java.util.stream.Collectors;

@UtilityClass
public final class ExchangeRateProviderUtils {

    public static String commaSeparatedCurrencyCodes(Collection<Currency> currencies) {
        return currencies.stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.joining(","));
    }

    public static Currency currencyOrNull(String code) {
        if (code == null) {
            return null;
        }

        try {
            return Currency.getInstance(code);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static RuntimeException providerException(ExchangeRateProviderName provider, RuntimeException ex) {
        if (ex instanceof CurrencyNotFoundException || ex instanceof ExternalProviderException) {
            return ex;
        }
        if (ex instanceof FeignException feignException) {
            if (feignException.status() == 404) {
                return new CurrencyNotFoundException(provider + " did not find the requested currency or rate");
            }
            return new ExternalProviderException(provider + " returned HTTP " + feignException.status(), feignException);
        }
        return ex;
    }
}

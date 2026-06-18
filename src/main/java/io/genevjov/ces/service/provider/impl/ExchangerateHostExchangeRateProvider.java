package io.genevjov.ces.service.provider.impl;

import io.genevjov.ces.client.ExchangerateHostFeignClient;
import io.genevjov.ces.client.dto.ExchangerateHostErrorResponse;
import io.genevjov.ces.client.dto.ExchangerateHostLiveResponse;
import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.exception.ExternalProviderException;
import io.genevjov.ces.mapper.ExchangerateHostExchangeRateMapper;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import io.genevjov.ces.service.provider.ExchangeRateProvider;
import io.genevjov.ces.utils.ExchangeRateProviderUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Currency;

import static io.genevjov.ces.enums.ExchangeRateProviderName.EXCHANGERATE_HOST;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ExchangerateHostExchangeRateProvider implements ExchangeRateProvider {

    private static final ExchangeRateProviderName PROVIDER = EXCHANGERATE_HOST;

    ExchangerateHostFeignClient feignClient;
    ExchangerateHostExchangeRateMapper mapper;

    @Override
    public ExchangeRateProviderName getProvider() {
        return PROVIDER;
    }

    @Override
    public ExchangeRatesSnapshot getLatestRates(Currency base, Collection<Currency> targets) {
        try {
            ExchangerateHostLiveResponse response = CollectionUtils.isEmpty(targets)
                    ? feignClient.latestRates(base)
                    : feignClient.latestRates(
                    base,
                    ExchangeRateProviderUtils.commaSeparatedCurrencyCodes(targets));

            validateResponse(base, response);

            return mapper.toSnapshot(base, response, PROVIDER);
        } catch (RuntimeException ex) {
            throw ExchangeRateProviderUtils.providerException(PROVIDER, ex);
        }
    }

    private void validateResponse(Currency base, ExchangerateHostLiveResponse response) {
        if (response == null) {
            throw new ExternalProviderException("Provider returned an empty response");
        }

        if (!response.success()) {
            throw new ExternalProviderException("Provider rejected the request: " + errorMessage(response.error()));
        }

        if (CollectionUtils.isEmpty(response.quotes())) {
            throw new CurrencyNotFoundException("No exchange rates returned for " + base.getCurrencyCode());
        }
    }

    private String errorMessage(ExchangerateHostErrorResponse error) {
        var unknownErrorMessage = "unknown provider error";

        if (error == null) {
            return unknownErrorMessage;
        }

        if (StringUtils.hasText(error.info())) {
            return error.info();
        }

        if (StringUtils.hasText(error.type())) {
            return error.type();
        }

        return unknownErrorMessage;
    }
}

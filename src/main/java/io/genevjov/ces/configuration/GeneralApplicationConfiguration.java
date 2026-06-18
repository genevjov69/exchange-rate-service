package io.genevjov.ces.configuration;

import io.genevjov.ces.enums.ExchangeRateProviderName;
import io.genevjov.ces.service.provider.ExchangeRateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class GeneralApplicationConfiguration {

    @Bean
    public Map<ExchangeRateProviderName, ExchangeRateProvider> exchangeRateProviderContainer(List<ExchangeRateProvider> providers) {
        return providers.stream()
                .collect(Collectors.toMap(
                        ExchangeRateProvider::getProvider,
                        Function.identity()));
    }
}

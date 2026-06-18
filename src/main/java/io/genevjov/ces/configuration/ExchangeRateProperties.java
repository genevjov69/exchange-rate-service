package io.genevjov.ces.configuration;

import io.genevjov.ces.enums.ExchangeRateProviderName;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "currency-exchange-service.configuration.exchange-rates")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class ExchangeRateProperties {

    @Getter
    @Setter
    List<ExchangeRateProviderName> providerPriority;
}

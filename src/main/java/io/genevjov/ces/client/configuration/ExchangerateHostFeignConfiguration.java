package io.genevjov.ces.client.configuration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

public class ExchangerateHostFeignConfiguration {

    @Bean
    RequestInterceptor exchangerateHostAccessKeyInterceptor(
            @Value("${currency-exchange-service.configuration.exchange-rates.providers.EXCHANGERATE_HOST.access-key}") String accessKey) {
        return template -> {
            if (StringUtils.hasText(accessKey)) {
                template.query("access_key", accessKey);
            }
        };
    }
}

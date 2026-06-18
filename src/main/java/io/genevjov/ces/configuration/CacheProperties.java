package io.genevjov.ces.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties(prefix = "currency-exchange-service.configuration.cache")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class CacheProperties {

    Duration ttl = Duration.ofMinutes(1);
}

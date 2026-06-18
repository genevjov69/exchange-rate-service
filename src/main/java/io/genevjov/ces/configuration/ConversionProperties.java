package io.genevjov.ces.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "currency-exchange-service.configuration")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class ConversionProperties {

    Integer conversionScale;

}

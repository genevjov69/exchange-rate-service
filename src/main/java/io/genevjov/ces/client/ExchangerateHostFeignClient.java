package io.genevjov.ces.client;

import io.genevjov.ces.client.dto.ExchangerateHostLiveResponse;
import io.genevjov.ces.client.configuration.ExchangerateHostFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Currency;

@FeignClient(
        name = "exchangerate-host",
        url = "${currency-exchange-service.configuration.exchange-rates.providers.EXCHANGERATE_HOST.base-url}",
        configuration = ExchangerateHostFeignConfiguration.class)
public interface ExchangerateHostFeignClient {

    @GetMapping("/live")
    ExchangerateHostLiveResponse latestRates(
            @RequestParam("source") Currency source);

    @GetMapping("/live")
    ExchangerateHostLiveResponse latestRates(
            @RequestParam("source") Currency source,
            @RequestParam("currencies") String currencies);
}

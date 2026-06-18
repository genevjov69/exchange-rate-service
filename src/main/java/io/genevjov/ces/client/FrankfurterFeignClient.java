package io.genevjov.ces.client;

import io.genevjov.ces.client.dto.FrankfurterRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Currency;
import java.util.List;

@FeignClient(
        name = "frankfurter",
        url = "${currency-exchange-service.configuration.exchange-rates.providers.FRANKFURTER.base-url}")
public interface FrankfurterFeignClient {

    @GetMapping("/v2/rates")
    List<FrankfurterRateResponse> latestRates(
            @RequestParam("base") Currency base);

    @GetMapping("/v2/rates")
    List<FrankfurterRateResponse> latestRates(
            @RequestParam("base") Currency base,
            @RequestParam("quotes") String quotes);
}

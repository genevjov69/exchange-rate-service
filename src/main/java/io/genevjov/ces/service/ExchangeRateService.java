package io.genevjov.ces.service;

import io.genevjov.ces.dto.response.BatchExchangeRatesResponse;
import io.genevjov.ces.dto.response.ExchangeRateResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Currency;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExchangeRateService {

    public ExchangeRateResponse getExchangeRate(Currency base, Currency target) {
        return null;
    }

    public BatchExchangeRatesResponse getAllExchangeRates(Currency base) {
        return null;
    }

}

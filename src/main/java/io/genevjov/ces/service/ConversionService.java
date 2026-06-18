package io.genevjov.ces.service;

import io.genevjov.ces.configuration.ConversionProperties;
import io.genevjov.ces.dto.request.BatchConversionRequest;
import io.genevjov.ces.dto.response.BatchConversionResponse;
import io.genevjov.ces.dto.response.ConversionResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ConversionService {

    ConversionProperties conversionProperties;

    public ConversionResponse convert(Currency from, Currency to, BigDecimal amount) {
        return null;
    }

    public BatchConversionResponse batchConvert(BatchConversionRequest request) {
        return null;
    }

    private BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(conversionProperties.getConversionScale(), RoundingMode.HALF_UP);
    }
}

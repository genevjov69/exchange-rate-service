package io.genevjov.ces.service;

import io.genevjov.ces.configuration.ConversionProperties;
import io.genevjov.ces.dto.request.BatchConversionRequest;
import io.genevjov.ces.dto.response.BatchConversionResponse;
import io.genevjov.ces.dto.response.ConversionResponse;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.exception.InvalidCurrencyException;
import io.genevjov.ces.mapper.ConversionResponseMapper;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Currency;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ConversionService {

    ConversionProperties conversionProperties;
    ExchangeRateService exchangeRateService;
    ConversionResponseMapper conversionResponseMapper;

    public ConversionResponse convert(Currency from, Currency to, BigDecimal amount) {
        validateAmount(amount);

        ExchangeRatesSnapshot snapshot = exchangeRateService.getExchangeRates(from, List.of(to));
        BigDecimal rate = rate(snapshot, to);
        BigDecimal convertedAmount = convert(amount, rate);

        return conversionResponseMapper.toConversionResponse(
                from,
                to,
                amount,
                rate,
                convertedAmount,
                snapshot);
    }

    public BatchConversionResponse batchConvert(BatchConversionRequest request) {
        validateAmount(request.amount());

        Currency from = request.from();
        Set<Currency> targets = new LinkedHashSet<>(request.targets());

        ExchangeRatesSnapshot snapshot = exchangeRateService.getExchangeRates(from, targets);
        List<BatchConversionResponse.ConversionItemResponse> conversions = targets.stream()
                .map(target -> {
                    BigDecimal rate = rate(snapshot, target);
                    return conversionResponseMapper.toConversionItemResponse(target, rate, convert(request.amount(), rate));
                })
                .toList();

        return conversionResponseMapper.toBatchConversionResponse(
                from,
                request.amount(),
                conversions,
                snapshot);
    }

    private BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(conversionProperties.getConversionScale(), RoundingMode.HALF_UP);
    }

    private BigDecimal rate(ExchangeRatesSnapshot snapshot, Currency target) {
        BigDecimal rate = snapshot.rates().get(target);
        if (rate == null) {
            throw new CurrencyNotFoundException("No exchange rate available for "
                    + snapshot.base().getCurrencyCode() + " -> " + target.getCurrencyCode());
        }
        return rate;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidCurrencyException("Amount must be positive");
        }
    }
}

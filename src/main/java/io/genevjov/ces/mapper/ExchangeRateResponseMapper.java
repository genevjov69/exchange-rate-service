package io.genevjov.ces.mapper;

import io.genevjov.ces.dto.response.BatchExchangeRatesResponse;
import io.genevjov.ces.dto.response.ExchangeRateResponse;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Currency;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ExchangeRateResponseMapper {

    @Mapping(target = "base", source = "snapshot.base")
    @Mapping(target = "provider", source = "snapshot.provider")
    @Mapping(target = "timestamp", source = "snapshot.timestamp")
    ExchangeRateResponse toExchangeRateResponse(ExchangeRatesSnapshot snapshot, Currency target, BigDecimal rate);

    @Mapping(target = "base", source = "snapshot.base")
    @Mapping(target = "rates", source = "snapshot.rates")
    @Mapping(target = "provider", source = "snapshot.provider")
    @Mapping(target = "timestamp", source = "snapshot.timestamp")
    BatchExchangeRatesResponse toBatchExchangeRatesResponse(ExchangeRatesSnapshot snapshot);
}

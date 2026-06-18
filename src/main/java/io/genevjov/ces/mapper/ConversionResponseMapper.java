package io.genevjov.ces.mapper;

import io.genevjov.ces.dto.response.BatchConversionResponse;
import io.genevjov.ces.dto.response.ConversionResponse;
import io.genevjov.ces.model.ExchangeRatesSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ConversionResponseMapper {

    @Mapping(target = "provider", source = "snapshot.provider")
    @Mapping(target = "timestamp", source = "snapshot.timestamp")
    ConversionResponse toConversionResponse(
            Currency from,
            Currency to,
            BigDecimal amount,
            BigDecimal rate,
            BigDecimal convertedAmount,
            ExchangeRatesSnapshot snapshot);

    BatchConversionResponse.ConversionItemResponse toConversionItemResponse(
            Currency to,
            BigDecimal rate,
            BigDecimal convertedAmount);

    @Mapping(target = "provider", source = "snapshot.provider")
    @Mapping(target = "timestamp", source = "snapshot.timestamp")
    BatchConversionResponse toBatchConversionResponse(
            Currency from,
            BigDecimal amount,
            List<BatchConversionResponse.ConversionItemResponse> conversions,
            ExchangeRatesSnapshot snapshot);
}

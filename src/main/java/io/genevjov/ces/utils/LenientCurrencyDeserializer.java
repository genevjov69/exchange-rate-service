package io.genevjov.ces.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Currency;

public class LenientCurrencyDeserializer extends JsonDeserializer<Currency> {

    @Override
    public Currency deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return ExchangeRateProviderUtils.currencyOrNull(parser.getValueAsString());
    }
}

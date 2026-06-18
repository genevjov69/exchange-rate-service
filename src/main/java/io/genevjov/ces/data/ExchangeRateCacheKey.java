package io.genevjov.ces.data;

import java.util.Currency;

public record ExchangeRateCacheKey(Currency base) {

    public static ExchangeRateCacheKey from(Currency base) {
        return new ExchangeRateCacheKey(base);
    }
}

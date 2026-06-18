package io.genevjov.ces.controller;

import io.genevjov.ces.dto.response.BatchExchangeRatesResponse;
import io.genevjov.ces.dto.response.ExchangeRateResponse;
import io.genevjov.ces.exception.CurrencyNotFoundException;
import io.genevjov.ces.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

import static io.genevjov.ces.enums.ExchangeRateProviderName.FRANKFURTER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Instant TIMESTAMP = Instant.parse("2026-06-18T00:00:00Z");

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ExchangeRateService exchangeRateService;

    @Test
    void returnsSingleExchangeRate() throws Exception {
        when(exchangeRateService.getExchangeRate(EUR, USD))
                .thenReturn(new ExchangeRateResponse(EUR, USD, BigDecimal.valueOf(1.1565), FRANKFURTER, TIMESTAMP));

        mockMvc.perform(get("/api/v1/exchange-rates/EUR/USD"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.base").value("EUR"))
                .andExpect(jsonPath("$.target").value("USD"))
                .andExpect(jsonPath("$.rate").value(1.1565))
                .andExpect(jsonPath("$.provider").value("FRANKFURTER"))
                .andExpect(jsonPath("$.timestamp").value("2026-06-18T00:00:00Z"));
    }

    @Test
    void returnsAllExchangeRatesForBase() throws Exception {
        when(exchangeRateService.getAllExchangeRates(EUR))
                .thenReturn(new BatchExchangeRatesResponse(
                        EUR,
                        Map.of(USD, BigDecimal.valueOf(1.1565), GBP, BigDecimal.valueOf(0.86515)),
                        FRANKFURTER,
                        TIMESTAMP));

        mockMvc.perform(get("/api/v1/exchange-rates/EUR"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.base").value("EUR"))
                .andExpect(jsonPath("$.rates.USD").value(1.1565))
                .andExpect(jsonPath("$.rates.GBP").value(0.86515))
                .andExpect(jsonPath("$.provider").value("FRANKFURTER"))
                .andExpect(jsonPath("$.timestamp").value("2026-06-18T00:00:00Z"));
    }

    @Test
    void mapsCurrencyNotFoundToNotFoundResponse() throws Exception {
        when(exchangeRateService.getExchangeRate(EUR, USD))
                .thenThrow(new CurrencyNotFoundException("No exchange rate available for EUR -> USD"));

        mockMvc.perform(get("/api/v1/exchange-rates/EUR/USD"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No exchange rate available for EUR -> USD"))
                .andExpect(jsonPath("$.path").value("/api/v1/exchange-rates/EUR/USD"));
    }
}

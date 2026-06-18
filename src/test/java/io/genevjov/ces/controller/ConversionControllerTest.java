package io.genevjov.ces.controller;

import io.genevjov.ces.dto.response.BatchConversionResponse;
import io.genevjov.ces.dto.response.ConversionResponse;
import io.genevjov.ces.service.ConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import static io.genevjov.ces.enums.ExchangeRateProviderName.FRANKFURTER;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversionController.class)
class ConversionControllerTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Instant TIMESTAMP = Instant.parse("2026-06-18T00:00:00Z");

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ConversionService conversionService;

    @Test
    void returnsSingleConversion() throws Exception {
        when(conversionService.convert(EUR, USD, BigDecimal.valueOf(100)))
                .thenReturn(new ConversionResponse(
                        EUR,
                        USD,
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(1.1565),
                        new BigDecimal("115.6500"),
                        FRANKFURTER,
                        TIMESTAMP));

        mockMvc.perform(get("/api/v1/conversions")
                        .param("from", "EUR")
                        .param("to", "USD")
                        .param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from").value("EUR"))
                .andExpect(jsonPath("$.to").value("USD"))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.rate").value(1.1565))
                .andExpect(jsonPath("$.convertedAmount").value(115.6500))
                .andExpect(jsonPath("$.provider").value("FRANKFURTER"))
                .andExpect(jsonPath("$.timestamp").value("2026-06-18T00:00:00Z"));
    }

    @Test
    void returnsBatchConversion() throws Exception {
        when(conversionService.batchConvert(argThat(request ->
                EUR.equals(request.from())
                        && BigDecimal.valueOf(100).compareTo(request.amount()) == 0
                        && request.targets().equals(List.of(USD, GBP)))))
                .thenReturn(new BatchConversionResponse(
                        EUR,
                        BigDecimal.valueOf(100),
                        List.of(
                                new BatchConversionResponse.ConversionItemResponse(
                                        USD,
                                        BigDecimal.valueOf(1.1565),
                                        new BigDecimal("115.6500")),
                                new BatchConversionResponse.ConversionItemResponse(
                                        GBP,
                                        BigDecimal.valueOf(0.86515),
                                        new BigDecimal("86.5150"))),
                        FRANKFURTER,
                        TIMESTAMP));

        mockMvc.perform(post("/api/v1/conversions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "from": "EUR",
                                  "amount": 100,
                                  "targets": ["USD", "GBP"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from").value("EUR"))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.conversions[0].to").value("USD"))
                .andExpect(jsonPath("$.conversions[0].rate").value(1.1565))
                .andExpect(jsonPath("$.conversions[0].convertedAmount").value(115.6500))
                .andExpect(jsonPath("$.conversions[1].to").value("GBP"))
                .andExpect(jsonPath("$.conversions[1].rate").value(0.86515))
                .andExpect(jsonPath("$.conversions[1].convertedAmount").value(86.5150))
                .andExpect(jsonPath("$.provider").value("FRANKFURTER"))
                .andExpect(jsonPath("$.timestamp").value("2026-06-18T00:00:00Z"));
    }

    @Test
    void rejectsInvalidBatchRequestBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/conversions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "from": "EUR",
                                  "amount": 100,
                                  "targets": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/conversions/batch"));
    }
}

package com.example.currency_rates;

import com.example.currency_rates.client.CBCurrencyResponse;
import com.example.currency_rates.controller.CurrencyController;
import com.example.currency_rates.dto.ConvertCurrencyRequest;
import com.example.currency_rates.dto.ConvertCurrencyResponse;
import com.example.currency_rates.dto.CurrencyRateResponse;
import com.example.currency_rates.exception.InvalidRequestException;
import com.example.currency_rates.services.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    private List<CBCurrencyResponse> mockCurrencies;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockCurrencies = new ArrayList<>();
        mockCurrencies.add(new CBCurrencyResponse("R01235", 840, "USD", 1, "Доллар США", "70.50"));
        mockCurrencies.add(new CBCurrencyResponse("R01239", 978, "EUR", 1, "Евро", "82.30"));
        mockCurrencies.add(new CBCurrencyResponse("R00000", 643, "RUB", 1, "Российский рубль", "1"));
    }

    @Test
    void testGetCurrencyRate_Success() throws Exception {
        CurrencyRateResponse expectedResponse = new CurrencyRateResponse("USD", new BigDecimal("70.50"));
        when(currencyService.getCurrencyRate("USD")).thenReturn(expectedResponse);

        mockMvc.perform(get("/currencies/rates/USD"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.rate").value("70.5"));
    }

    @Test
    void testGetCurrencyRate_NotFound() throws Exception {
        when(currencyService.getCurrencyRate("ABC")).thenThrow(new InvalidRequestException("Unsupported currency code - ABC"));

        mockMvc.perform(get("/currencies/rates/ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Unsupported currency code - ABC"));
    }

    @Test
    void testConvertCurrency_Success() throws Exception {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("USD", "RUB", new BigDecimal("100.00"));
        ConvertCurrencyResponse expectedResponse = new ConvertCurrencyResponse("USD", "RUB", "7450");

        when(currencyService.convertCurrency(request)).thenReturn(expectedResponse);

        mockMvc.perform(post("/currencies/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromCurrency\":\"USD\",\"toCurrency\":\"RUB\",\"amount\":100.00}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("RUB"))
                .andExpect(jsonPath("$.convertedAmount").value("7450"));
    }

    @Test
    void testConvertCurrency_InvalidRequest() throws Exception {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("USD", "INVALID", new BigDecimal("100.00"));
        when(currencyService.convertCurrency(request)).thenThrow(new InvalidRequestException("Unsupported currency code - INVALID"));

        mockMvc.perform(post("/currencies/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromCurrency\":\"USD\",\"toCurrency\":\"INVALID\",\"amount\":100.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Unsupported currency code - INVALID"));
    }
}
package com.example.currency_rates;

import com.example.currency_rates.client.CBCurrencyResponse;
import com.example.currency_rates.client.CurrencyClient;
import com.example.currency_rates.dto.ConvertCurrencyRequest;
import com.example.currency_rates.dto.ConvertCurrencyResponse;
import com.example.currency_rates.dto.CurrencyRateResponse;
import com.example.currency_rates.exception.InvalidRequestException;
import com.example.currency_rates.mapper.CBCurrencyResponseToCurrencyRateResponseMapper;
import com.example.currency_rates.services.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CurrencyServiceTest {

    @Mock
    private CurrencyClient currencyClient;

    @Mock
    private CBCurrencyResponseToCurrencyRateResponseMapper currencyMapper;

    @InjectMocks
    private CurrencyService currencyService;

    private List<CBCurrencyResponse> mockCurrencies;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockCurrencies = new ArrayList<>();
        mockCurrencies.add(new CBCurrencyResponse("R01235", "840", "USD", 1, "Доллар США", "70.50"));
        mockCurrencies.add(new CBCurrencyResponse("R01239", "978", "EUR", 1, "Евро", "82.30"));
        mockCurrencies.add(new CBCurrencyResponse("R00000", "643", "RUB", 1, "Российский рубль", "1"));
    }

    @Test
    void testGetCurrencyRate_Success() {
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        CurrencyRateResponse response = currencyService.getCurrencyRate("USD");

        assertNotNull(response);
        assertEquals("USD", response.getCurrency());
        assertEquals("70.50", response.getRate().toPlainString());
    }

    @Test
    void testGetCurrencyRate_CurrencyNotFound() {
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> currencyService.getCurrencyRate("ABC"));
        assertEquals("Unsupported currency code - ABC", exception.getMessage());
    }

    @Test
    void testConvertCurrency_Success_FromRUB() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("RUB", "USD", new BigDecimal("100.00"));
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        ConvertCurrencyResponse response = currencyService.convertCurrency(request);

        assertNotNull(response);
        assertEquals("RUB", response.getFromCurrency());
        assertEquals("USD", response.getToCurrency());
        assertEquals("1.42", response.getConvertedAmount()); // Убедитесь, что ожидаемое значение соответствует вашему расчету
    }

    @Test
    void testConvertCurrency_Success_ToRUB() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("USD", "RUB", new BigDecimal("50.00"));
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        ConvertCurrencyResponse response = currencyService.convertCurrency(request);

        assertNotNull(response);
        assertEquals("USD", response.getFromCurrency());
        assertEquals("RUB", response.getToCurrency());
        assertEquals("3525.00", response.getConvertedAmount()); // Проверка результата конвертации из USD в RUB
    }

    @Test
    void testConvertCurrency_BetweenForeignCurrencies() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("USD", "EUR", new BigDecimal("100.00"));
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        ConvertCurrencyResponse response = currencyService.convertCurrency(request);

        assertNotNull(response);
        assertEquals("USD", response.getFromCurrency());
        assertEquals("EUR", response.getToCurrency());
        assertEquals("85.66", response.getConvertedAmount()); // Убедитесь, что ожидаемое значение соответствует вашему расчету
    }

    @Test
    void testConvertCurrency_InvalidFromCurrency() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("INVALID", "EUR", new BigDecimal("100.00"));
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> currencyService.convertCurrency(request));
        assertEquals("Unsupported currency code - INVALID", exception.getMessage());
    }

    @Test
    void testConvertCurrency_InvalidToCurrency() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("RUB", "INVALID", new BigDecimal("100.00"));
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> currencyService.convertCurrency(request));
        assertEquals("Unsupported currency code - INVALID", exception.getMessage());
    }

    @Test
    void testConvertCurrency_NegativeAmount() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("RUB", "USD", new BigDecimal("-50.00"));
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> currencyService.convertCurrency(request));
        assertEquals("Amount must be greater than 0", exception.getMessage());
    }

    @Test
    void testConvertCurrency_ZeroAmount() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest("RUB", "USD", new BigDecimal("0.00"));
        when(currencyClient.getCurrencies()).thenReturn(mockCurrencies);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> currencyService.convertCurrency(request));
        assertEquals("Amount must be greater than 0", exception.getMessage());
    }
}

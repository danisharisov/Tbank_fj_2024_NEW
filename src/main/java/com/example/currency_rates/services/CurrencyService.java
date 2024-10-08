package com.example.currency_rates.services;

import com.example.currency_rates.client.CBCurrencyResponse;
import com.example.currency_rates.client.CurrencyClient;
import com.example.currency_rates.dto.ConvertCurrencyRequest;
import com.example.currency_rates.dto.ConvertCurrencyResponse;
import com.example.currency_rates.dto.CurrencyRateResponse;
import com.example.currency_rates.exception.CurrencyNotFoundException;
import com.example.currency_rates.exception.InvalidRequestException;
import com.example.currency_rates.exception.ServiceException;
import com.example.currency_rates.mapper.CBCurrencyResponseToCurrencyRateResponseMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

@Service
public class CurrencyService {

    private static final String RUB_CODE = "RUB";

    private final CurrencyClient currencyClient;
    private final CBCurrencyResponseToCurrencyRateResponseMapper currencyMapper;

    public CurrencyService(CurrencyClient currencyClient, CBCurrencyResponseToCurrencyRateResponseMapper currencyMapper) {
        this.currencyClient = currencyClient;
        this.currencyMapper = currencyMapper;
    }

    @Cacheable(value = "currencyRates", key = "#code", unless = "#result == null")
    public CurrencyRateResponse getCurrencyRate(String code) {
        validateCurrencyCode(code);

        try {
            List<CBCurrencyResponse> currencies = currencyClient.getCurrencies();
            CBCurrencyResponse currency = currencies.stream()
                    .filter(c -> Objects.equals(c.getCharCode(), code))
                    .findFirst()
                    .orElseThrow(() -> new CurrencyNotFoundException("Currency not found - " + code));

            return new CurrencyRateResponse(
                    currency.getCharCode(),
                    new BigDecimal(currency.getValue().replace(",", "."))
            );
        } catch (Exception e) {
            throw new ServiceException("Error retrieving currency rates from the service", e);
        }
    }

    public ConvertCurrencyResponse convertCurrency(ConvertCurrencyRequest request) {
        validateConvertRequest(request);

        var currencies = currencyClient.getCurrencies();

        CBCurrencyResponse fromCurrency;
        CBCurrencyResponse toCurrency;

        if (RUB_CODE.equals(request.getFromCurrency())) {
            toCurrency = getCurrencyByCode(request.getToCurrency(), currencies);
            BigDecimal toRate = new BigDecimal(toCurrency.getValue().replace(",", "."));
            BigDecimal convertedAmount = request.getAmount().divide(toRate, 4, RoundingMode.HALF_UP);
            return createConvertCurrencyResponse(request, convertedAmount);
        }

        if (RUB_CODE.equals(request.getToCurrency())) {
            fromCurrency = getCurrencyByCode(request.getFromCurrency(), currencies);
            BigDecimal fromRate = new BigDecimal(fromCurrency.getValue().replace(",", "."));
            BigDecimal convertedAmount = request.getAmount().multiply(fromRate);
            return createConvertCurrencyResponse(request, convertedAmount);
        }

        fromCurrency = getCurrencyByCode(request.getFromCurrency(), currencies);
        toCurrency = getCurrencyByCode(request.getToCurrency(), currencies);

        BigDecimal fromRateInRub = new BigDecimal(fromCurrency.getValue().replace(",", "."));
        BigDecimal toRateInRub = new BigDecimal(toCurrency.getValue().replace(",", "."));

        BigDecimal convertedAmount = request.getAmount().multiply(fromRateInRub).divide(toRateInRub, 4, RoundingMode.HALF_UP);

        return createConvertCurrencyResponse(request, convertedAmount);
    }




    private void validateConvertRequest(ConvertCurrencyRequest request) {
        if (request.getFromCurrency() == null || request.getToCurrency() == null || request.getAmount() == null) {
            throw new InvalidRequestException("Missing required parameters");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than 0");
        }
        validateCurrencyCode(request.getFromCurrency());
        validateCurrencyCode(request.getToCurrency());
    }



    private void validateCurrencyCode(String code) {
        if (RUB_CODE.equals(code)) {
            return;
        }
        try {
            Currency currency = Currency.getInstance(code);
            List<CBCurrencyResponse> currencies = currencyClient.getCurrencies();
            boolean currencyExists = currencies.stream().anyMatch(c -> c.getCharCode().equals(currency.getCurrencyCode()));
            if (!currencyExists) {
                throw new CurrencyNotFoundException("Currency not found - " + code);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Unsupported currency code - " + code);
        }
    }


    private CBCurrencyResponse getCurrencyByCode(String code, List<CBCurrencyResponse> currencies) {
        return currencies.stream()
                .filter(cbCurrencyResponse -> Objects.equals(cbCurrencyResponse.getCharCode(), code))
                .findFirst()
                .orElseThrow(
                        () -> new CurrencyNotFoundException("Currency not found - " + code)
                );
    }

    private ConvertCurrencyResponse createConvertCurrencyResponse(ConvertCurrencyRequest request, BigDecimal convertedAmount) {
        return new ConvertCurrencyResponse(
                request.getFromCurrency(),
                request.getToCurrency(),
                convertedAmount.setScale(2, RoundingMode.HALF_UP).toPlainString()
        );
    }
}

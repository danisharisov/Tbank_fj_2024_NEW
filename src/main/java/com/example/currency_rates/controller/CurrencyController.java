package com.example.currency_rates.controller;

import com.example.currency_rates.dto.ConvertCurrencyRequest;
import com.example.currency_rates.dto.ConvertCurrencyResponse;
import com.example.currency_rates.dto.CurrencyRateResponse;
import com.example.currency_rates.services.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/currencies")
public class CurrencyController implements CurrencyApi {

    private final CurrencyService currencyService;

    @Override
    public CurrencyRateResponse getCurrencyRate(String code) {
        return currencyService.getCurrencyRate(code);
    }

    @Override
    public ConvertCurrencyResponse convertCurrency(ConvertCurrencyRequest request) {
        return currencyService.convertCurrency(request);
    }
}
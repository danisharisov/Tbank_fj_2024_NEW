package com.example.currency_rates.dto;

import lombok.Data;

@Data
public class BaseCurrencyConvertRequest {
    protected String fromCurrency;
    protected String toCurrency;
}
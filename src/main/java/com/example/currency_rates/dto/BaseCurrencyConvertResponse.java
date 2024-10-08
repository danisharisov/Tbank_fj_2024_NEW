package com.example.currency_rates.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseCurrencyConvertResponse {
    protected String fromCurrency;
    protected String toCurrency;
    protected String conversionDetails;
}

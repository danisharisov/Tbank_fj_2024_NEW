package com.example.currency_rates.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertCurrencyRequest extends BaseCurrencyConvertRequest {
    @NotBlank(message = "fromCurrency cannot be blank")
    private String fromCurrency;

    @NotBlank(message = "toCurrency cannot be blank")
    private String toCurrency;

    @DecimalMin(value = "0.1", message = "Amount should be greater than 0")
    private BigDecimal amount;
}
package com.example.currency_rates.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorType {
    INVALID_CURRENCY_CODE(400, "Invalid currency code"),
    CURRENCY_NOT_FOUND(404, "Currency not found"),
    SERVICE_UNAVAILABLE(503, "Service unavailable");

    private final int code;
    private final String message;


}
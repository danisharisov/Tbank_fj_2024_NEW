package com.example.currency_rates.controller;

import com.example.currency_rates.dto.ConvertCurrencyRequest;
import com.example.currency_rates.dto.ConvertCurrencyResponse;
import com.example.currency_rates.dto.CurrencyRateResponse;
import com.example.currency_rates.exception.CustomErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/currencies")
public interface CurrencyApi {

    @Operation(summary = "Получить курс валюты по коду")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Курс успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrencyRateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный код валюты",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Валюта не найдена в ЦБ",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Сервис недоступен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @GetMapping("/rates/{code}")
    CurrencyRateResponse getCurrencyRate(@PathVariable String code);

    @Operation(summary = "Конвертировать валюту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Конвертация успешно выполнена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConvertCurrencyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос на конвертацию валюты",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Валюта не найдена в ЦБ",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Сервис недоступен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    @PostMapping("/convert")
    ConvertCurrencyResponse convertCurrency(@RequestBody ConvertCurrencyRequest request);
}
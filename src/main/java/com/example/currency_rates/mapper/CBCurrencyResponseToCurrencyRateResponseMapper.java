package com.example.currency_rates.mapper;


import com.example.currency_rates.client.CBCurrencyResponse;
import com.example.currency_rates.dto.CurrencyRateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.core.convert.converter.Converter;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CBCurrencyResponseToCurrencyRateResponseMapper extends Converter<CBCurrencyResponse, CurrencyRateResponse> {
    @Override
    @Mapping(target = "currency", source = "charCode")
    @Mapping(target = "rate", source = "value")
    CurrencyRateResponse convert(CBCurrencyResponse source);
}
package com.example.currency_rates.client;

import com.example.currency_rates.exception.ServiceException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.List;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
@Component
public class CurrencyClient {
    private final RestTemplate restTemplate;
    private final String cbrFullUrl;
    private final XmlMapper xmlMapper;

    public CurrencyClient(RestTemplate restTemplate, @Value("${currency.cbr-url}") String cbrFullUrl, XmlMapper xmlMapper) {
        this.restTemplate = restTemplate;
        this.cbrFullUrl = cbrFullUrl;
        this.xmlMapper = xmlMapper;
    }

    @CircuitBreaker(name = "cbrService", fallbackMethod = "fallbackGetCurrencies")
    @Cacheable(value = "currencyList", unless = "#result == null")
    public List<CBCurrencyResponse> getCurrencies() {
        String xmlResponse = restTemplate.getForObject(cbrFullUrl, String.class);
        return parseCurrencies(xmlResponse);
    }

    public List<CBCurrencyResponse> fallbackGetCurrencies(Throwable ex) {
        System.err.println("Error occurred while fetching currencies: " + ex.getMessage());
        throw new ServiceException("ЦБ сервис недоступен, попробуйте позже");
    }

    private List<CBCurrencyResponse> parseCurrencies(String xmlResponse) {
        System.out.println("Received XML Response: " + xmlResponse);
        try {
            ValCurs valCurs = xmlMapper.readValue(xmlResponse.getBytes(StandardCharsets.UTF_8), ValCurs.class);
            return valCurs.getValutes();
        } catch (Exception e) {
            e.printStackTrace(); // Логируем стек вызовов
            throw new ServiceException("Ошибка при парсинге ответа от ЦБ РФ", e);
        }
    }
}
package com.example.currency_rates.client;

import com.example.currency_rates.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class CurrencyClient {
    private final RestTemplate restTemplate;
    private final String cbrFullUrl;

    public CurrencyClient(RestTemplate restTemplate, @Value("${currency.cbr-url}") String cbrFullUrl) {
        this.restTemplate = restTemplate;
        this.cbrFullUrl = cbrFullUrl;
    }

    @CircuitBreaker(name = "cbrService", fallbackMethod = "fallbackGetCurrencies")
    public List<CBCurrencyResponse> getCurrencies() {
        String xmlResponse = restTemplate.getForObject(cbrFullUrl, String.class);
        return parseCurrencies(xmlResponse);
    }

    public List<CBCurrencyResponse> fallbackGetCurrencies(Throwable ex) {
        throw new ServiceException("ЦБ сервис недоступен, попробуйте позже");
    }

    private List<CBCurrencyResponse> parseCurrencies(String xmlResponse) {
        List<CBCurrencyResponse> currencyList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));

            NodeList currencyNodes = document.getElementsByTagName("Valute");
            for (int i = 0; i < currencyNodes.getLength(); i++) {
                Element element = (Element) currencyNodes.item(i);
                CBCurrencyResponse currencyResponse = new CBCurrencyResponse();
                currencyResponse.setId(element.getAttribute("ID"));
                currencyResponse.setNumCode(Integer.parseInt(element.getElementsByTagName("NumCode").item(0).getTextContent()));
                currencyResponse.setCharCode(element.getElementsByTagName("CharCode").item(0).getTextContent());
                currencyResponse.setNominal(Integer.parseInt(element.getElementsByTagName("Nominal").item(0).getTextContent()));
                currencyResponse.setName(element.getElementsByTagName("Name").item(0).getTextContent());
                String value = element.getElementsByTagName("Value").item(0).getTextContent().replace(",", ".");
                currencyResponse.setValue(value);

                currencyList.add(currencyResponse);
            }
        } catch (Exception e) {
            throw new ServiceException("Ошибка при парсинге ответа от ЦБ РФ", e);
        }
        return currencyList;
    }
}
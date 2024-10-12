package com.example.currency_rates;

import com.example.currency_rates.client.CurrencyClient;
import com.example.currency_rates.exception.ServiceException;
import com.example.currency_rates.client.CBCurrencyResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
@Testcontainers
@SpringBootTest(properties = {"spring.profiles.active=test"})
public class CurrencyClientTest {

    @Container
    private static final GenericContainer<?> wireMockContainer = new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:2.35.0"))
            .withExposedPorts(8080)
            .withCopyFileToContainer(MountableFile.forClasspathResource("__files/currency.xml"), "/home/wiremock/__files/currency.xml")
            .withCopyFileToContainer(MountableFile.forClasspathResource("mappings/currency-mapping.json"), "/home/wiremock/mappings/currency-mapping.json")
            .withCommand("--local-response-templating --verbose --port 8080");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("currency.cbr-url", () -> "http://localhost:" + wireMockContainer.getMappedPort(8080) + "/scripts/XML_daily.asp");
    }

    @Autowired
    private CurrencyClient currencyClient;

    private static RestTemplate restTemplate = new RestTemplate();

    @BeforeAll
    public static void setUpWireMock() throws Exception {
        String baseUrl = "http://localhost:" + wireMockContainer.getMappedPort(8080);

        ClassLoader classLoader = CurrencyClientTest.class.getClassLoader();
        File xmlMappingFile = new File(classLoader.getResource("mappings/currency-mapping.json").getFile());
        String xmlMapping = new String(Files.readAllBytes(xmlMappingFile.toPath()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(xmlMapping, headers);

        var response = restTemplate.postForEntity(baseUrl + "/__admin/mappings", request, String.class);
        assertEquals(201, response.getStatusCodeValue(), "WireMock mapping creation failed for XML");
    }

    @AfterAll
    public static void tearDown() {
        wireMockContainer.stop();
    }

    @Test
    public void testGetAllCurrencies_ShouldReturnCurrencyMap() {
        List<CBCurrencyResponse> currencies = currencyClient.getCurrencies();

        assertNotNull(currencies, "Currencies list should not be null");
        assertEquals(2, currencies.size(), "Currencies list size should be 2");
        assertEquals("USD", currencies.get(0).getCharCode(), "First currency should be USD");
        assertEquals(new BigDecimal("94.8700"), new BigDecimal(currencies.get(0).getValue().replace(",", ".")), "USD rate should be 94.8700");
        assertEquals("EUR", currencies.get(1).getCharCode(), "Second currency should be EUR");
        assertEquals(new BigDecimal("104.7424"), new BigDecimal(currencies.get(1).getValue().replace(",", ".")), "EUR rate should be 104.7424");
    }

    @Test
    public void testGetCurrencies_ServiceUnavailable() {
        // Эмулируем недоступность сервиса
        wireMockContainer.stop(); // Остановка контейнера WireMock

        ServiceException exception = assertThrows(ServiceException.class, () -> currencyClient.getCurrencies());
        assertEquals("ЦБ сервис недоступен, попробуйте позже", exception.getMessage());
    }
}

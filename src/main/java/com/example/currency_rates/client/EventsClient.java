package com.example.currency_rates.client;

import com.example.currency_rates.config.KudaGoConfig;
import com.example.currency_rates.dto.EventResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Semaphore;

@Component
public class EventsClient {

    private final RestTemplate restTemplate;
    protected final Semaphore semaphore;
    private String eventApiUrl;

    public EventsClient(RestTemplate restTemplate, KudaGoConfig properties) {
        this.restTemplate = restTemplate;
        this.semaphore = new Semaphore(properties.getMaxConcurrentRequests());
        this.eventApiUrl = properties.getEUrl();
    }

    public EventResponse getEvents(long dateFrom, long dateTo) {
        try {
            semaphore.acquire();
            String url = String.format("%s?fields=id,title,price&order_by=-price&text_format=plain&actual_since=%d&actual_until=%d",
                    eventApiUrl, dateFrom, dateTo);
            return restTemplate.getForObject(url, EventResponse.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted", e);
        } finally {
            semaphore.release();
        }
    }
}
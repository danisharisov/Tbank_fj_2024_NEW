package com.example.currency_rates;

import com.example.currency_rates.client.EventsClient;
import com.example.currency_rates.config.KudaGoConfig;
import com.example.currency_rates.dto.Event;
import com.example.currency_rates.dto.EventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class EventsClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KudaGoConfig kudaGoConfig;

    @InjectMocks
    private EventsClient eventsClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(kudaGoConfig.getMaxConcurrentRequests()).thenReturn(8);
        when(kudaGoConfig.getEUrl()).thenReturn("https://kudago.com/public-api/v1.4/events/");
    }

    @Test
    void getEvents_ShouldRespectRateLimitWithMultipleThreads() throws InterruptedException {
        EventResponse mockResponse = new EventResponse();
        mockResponse.setCount(1);
        mockResponse.setResults(List.of(new Event(1, "Event 1", false, "500", 500.0)));

        String expectedUrl = String.format("%s?fields=id,title,price&order_by=-price&text_format=plain&actual_since=%d&actual_until=%d",
                "https://kudago.com/public-api/v1.4/events/", 1L, 2L);

        when(restTemplate.getForObject(eq(expectedUrl), eq(EventResponse.class))).thenReturn(mockResponse);

        int numberOfThreads = 4;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successfulRequests = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    EventResponse response = eventsClient.getEvents(1L, 2L);
                    if (response != null) {
                        successfulRequests.incrementAndGet();
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertEquals(numberOfThreads, successfulRequests.get(), "Not all requests were successful");
    }
}
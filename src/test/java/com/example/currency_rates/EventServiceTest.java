package com.example.currency_rates;

import com.example.currency_rates.client.EventsClient;
import com.example.currency_rates.dto.CurrencyRateResponse;
import com.example.currency_rates.dto.Event;
import com.example.currency_rates.dto.EventResponse;
import com.example.currency_rates.exception.ServiceException;
import com.example.currency_rates.services.CurrencyService;
import com.example.currency_rates.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Mock
    private EventsClient eventApi;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFilteredEvents_ShouldReturnFilteredEvents_WhenSuccessful() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        Event event1 = new Event(1, "Event 1", false, "500", 500.0);
        Event event2 = new Event(2, "Event 2", false, "2000", 2000.0);
        List<Event> eventList = Arrays.asList(event1, event2);

        EventResponse eventResponse = new EventResponse();
        eventResponse.setCount(eventList.size());
        eventResponse.setResults(eventList);

        when(eventApi.getEvents(any(Long.class), any(Long.class))).thenReturn(eventResponse);

        CurrencyRateResponse currencyRateResponse = new CurrencyRateResponse();
        currencyRateResponse.setCurrency(currency);
        currencyRateResponse.setRate(BigDecimal.valueOf(1.0));

        when(currencyService.getCurrencyRate(currency)).thenReturn(currencyRateResponse);

        CompletableFuture<EventResponse> result = eventService.getFilteredEvents(budget, currency, dateFrom, dateTo);

        EventResponse response = result.join();
        assertNotNull(response);
        assertEquals(1, response.getCount());
    }

    @Test
    void getEvents_ShouldThrowServiceException_WhenNoEventsFound() {
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        when(eventApi.getEvents(any(Long.class), any(Long.class))).thenReturn(new EventResponse());

        CompletionException exception = assertThrows(CompletionException.class, () -> {
            eventService.getEvents(dateFrom, dateTo).join();
        });

        assertTrue(exception.getCause() instanceof ServiceException);
        assertEquals("Failed to fetch events", exception.getCause().getMessage());
    }


    @Test
    void getFilteredEvents_ShouldFilterFreeEventsCorrectly() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        Event event1 = new Event(1, "Event 1", true, "0", 0.0);
        Event event2 = new Event(2, "Event 2", false, "1500", 1500.0);
        List<Event> eventList = Arrays.asList(event1, event2);

        EventResponse eventResponse = new EventResponse();
        eventResponse.setCount(eventList.size());
        eventResponse.setResults(eventList);

        when(eventApi.getEvents(any(Long.class), any(Long.class))).thenReturn(eventResponse);

        CurrencyRateResponse currencyRateResponse = new CurrencyRateResponse();
        currencyRateResponse.setCurrency(currency);
        currencyRateResponse.setRate(BigDecimal.valueOf(1.0));

        when(currencyService.getCurrencyRate(currency)).thenReturn(currencyRateResponse);

        CompletableFuture<EventResponse> result = eventService.getFilteredEvents(budget, currency, dateFrom, dateTo);

        EventResponse response = result.join();
        assertEquals(1, response.getCount());
    }
}

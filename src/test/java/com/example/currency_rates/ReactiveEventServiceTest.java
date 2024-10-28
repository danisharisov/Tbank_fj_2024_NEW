package com.example.currency_rates;

import com.example.currency_rates.client.EventsClient;
import com.example.currency_rates.dto.CurrencyRateResponse;
import com.example.currency_rates.dto.Event;
import com.example.currency_rates.dto.EventResponse;
import com.example.currency_rates.exception.ServiceException;
import com.example.currency_rates.services.CurrencyService;
import com.example.currency_rates.services.ReactiveEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReactiveEventServiceTest {

    @Mock
    private EventsClient eventApi;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private ReactiveEventService reactiveEventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFilteredEvents_ShouldReturnEventResponse_WhenSuccessful() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        Event event1 = new Event(1, "Event 1", false, "500", 500.0);
        Event event2 = new Event(2, "Event 2", false, "1500", 1500.0);

        EventResponse mockResponse = new EventResponse();
        mockResponse.setCount(1);
        mockResponse.setResults(Collections.singletonList(event1));

        when(eventApi.getEvents(any(Long.class), any(Long.class))).thenReturn(mockResponse);
        when(currencyService.getCurrencyRate(currency)).thenReturn(new CurrencyRateResponse(currency, BigDecimal.valueOf(1.0)));

        Mono<EventResponse> resultMono = reactiveEventService.getFilteredEvents(budget, currency, dateFrom, dateTo);
        EventResponse resultResponse = resultMono.block();

        assertNotNull(resultResponse);
        assertEquals(1, resultResponse.getCount());
    }

    @Test
    void getFilteredEvents_ShouldHandleServiceException_WhenEventsNotFound() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        when(eventApi.getEvents(any(Long.class), any(Long.class))).thenReturn(new EventResponse());
        when(currencyService.getCurrencyRate(currency)).thenReturn(new CurrencyRateResponse(currency, BigDecimal.valueOf(1.0)));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            reactiveEventService.getFilteredEvents(budget, currency, dateFrom, dateTo).block();
        });

        assertEquals("Failed to filter events", exception.getMessage());
    }

    @Test
    void getFilteredEvents_ShouldFilterFreeEventsCorrectly() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        Event freeEvent = new Event(1, "Free Event", true, "0", 0.0);
        Event paidEvent = new Event(2, "Paid Event", false, "1500", 1500.0);
        List<Event> eventList = Arrays.asList(freeEvent, paidEvent);

        EventResponse mockResponse = new EventResponse();
        mockResponse.setCount(eventList.size());
        mockResponse.setResults(eventList);

        when(eventApi.getEvents(any(Long.class), any(Long.class))).thenReturn(mockResponse);
        when(currencyService.getCurrencyRate(currency)).thenReturn(new CurrencyRateResponse(currency, BigDecimal.valueOf(1.0)));

        Mono<EventResponse> resultMono = reactiveEventService.getFilteredEvents(budget, currency, dateFrom, dateTo);
        EventResponse resultResponse = resultMono.block();

        assertEquals(1, resultResponse.getCount());
    }
}
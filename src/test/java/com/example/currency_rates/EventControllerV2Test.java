package com.example.currency_rates;

import com.example.currency_rates.controller.EventControllerV2;
import com.example.currency_rates.dto.Event;
import com.example.currency_rates.dto.EventResponse;
import com.example.currency_rates.services.ReactiveEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventControllerV2Test {

    @Mock
    private ReactiveEventService eventService;

    @InjectMocks
    private EventControllerV2 eventController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getEvents_ShouldReturnEvents_WhenValidRequest() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        Event event = new Event(1, "Event 1", false, "500", 500.0);
        EventResponse eventResponse = new EventResponse();
        eventResponse.setCount(1);
        eventResponse.setResults(Collections.singletonList(event));

        when(eventService.getFilteredEvents(budget, currency, dateFrom, dateTo))
                .thenReturn(Mono.just(eventResponse));

        Mono<ResponseEntity<List<EventResponse>>> result = eventController.getEvents(String.valueOf(budget), currency, dateFrom, dateTo);
        ResponseEntity<List<EventResponse>> responseEntity = result.block();
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals(1, responseEntity.getBody().size());
        assertEquals("Event 1", responseEntity.getBody().get(0).getResults().get(0).getTitle());
    }

    @Test
    void getEvents_ShouldReturnNotFound_WhenNoEventsFound() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        when(eventService.getFilteredEvents(budget, currency, dateFrom, dateTo))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<List<EventResponse>>> result = eventController.getEvents(String.valueOf(budget), currency, dateFrom, dateTo);

        ResponseEntity<List<EventResponse>> responseEntity = result.block();
        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    @Test
    void getEvents_ShouldThrowBadRequest_WhenBudgetIsInvalid() {
        String budget = "invalid";
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        Mono<ResponseEntity<List<EventResponse>>> result = eventController.getEvents(budget, currency, dateFrom, dateTo);

        ResponseEntity<List<EventResponse>> responseEntity = result.block();
        assertNotNull(responseEntity);
        assertEquals(400, responseEntity.getStatusCodeValue());
    }
}
package com.example.currency_rates;
import com.example.currency_rates.controller.EventController;
import com.example.currency_rates.dto.EventResponse;
import com.example.currency_rates.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getEvents_ShouldReturnEventResponse_WhenSuccessful() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        EventResponse mockResponse = new EventResponse();
        mockResponse.setCount(2);

        when(eventService.getFilteredEvents(budget, currency, dateFrom, dateTo)).thenReturn(CompletableFuture.completedFuture(mockResponse));

        CompletableFuture<ResponseEntity<EventResponse>> futureResponse = eventController.getEvents(budget, currency, dateFrom, dateTo);
        ResponseEntity<EventResponse> response = futureResponse.join();
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void getEvents_ShouldHandleServiceException() {
        double budget = 1000.0;
        String currency = "rub";
        LocalDate dateFrom = LocalDate.now().minusDays(7);
        LocalDate dateTo = LocalDate.now();

        when(eventService.getFilteredEvents(budget, currency, dateFrom, dateTo)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service error")));

        CompletableFuture<ResponseEntity<EventResponse>> futureResponse = eventController.getEvents(budget, currency, dateFrom, dateTo);

        assertThrows(RuntimeException.class, futureResponse::join);
    }
}

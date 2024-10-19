package com.example.currency_rates.controller;


import com.example.currency_rates.dto.EventResponse;
import com.example.currency_rates.services.EventService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @GetMapping
    public CompletableFuture<ResponseEntity<EventResponse>> getEvents(
            @RequestParam double budget,
            @RequestParam String currency,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        logger.info("Received request to get events with budget: {}, currency: {}, dateFrom: {}, dateTo: {}", budget, currency, dateFrom, dateTo);
        return eventService.getFilteredEvents(budget, currency, dateFrom, dateTo)
                .thenApply(ResponseEntity::ok);
    }
}

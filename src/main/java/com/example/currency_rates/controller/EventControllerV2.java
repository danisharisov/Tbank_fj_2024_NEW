package com.example.currency_rates.controller;

import com.example.currency_rates.dto.EventResponse;
import com.example.currency_rates.services.ReactiveEventService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/events/v2")
@RequiredArgsConstructor
public class EventControllerV2 {
    private final ReactiveEventService eventService;
    private static final Logger logger = LoggerFactory.getLogger(EventControllerV2.class);

    @GetMapping
    public Mono<ResponseEntity<List<EventResponse>>> getEvents(
            @RequestParam String budget,
            @RequestParam String currency,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {

        logger.info("Received request to get events with budget: {}, currency: {}, dateFrom: {}, dateTo: {}", budget, currency, dateFrom, dateTo);

        return eventService.getFilteredEvents(Double.parseDouble(budget), currency, dateFrom, dateTo)
                .flatMap(eventResponse -> {
                    List<EventResponse> eventList = Collections.singletonList(eventResponse);
                    return Mono.just(ResponseEntity.ok(eventList));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

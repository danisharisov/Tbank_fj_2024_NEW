package com.example.currency_rates.services;

import com.example.currency_rates.client.EventsClient;
import com.example.currency_rates.dto.CurrencyRateResponse;
import com.example.currency_rates.dto.Event;
import com.example.currency_rates.dto.EventResponse;
import com.example.currency_rates.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final EventsClient eventApi;
    private final CurrencyService currencyService;
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private static final Pattern PRICE_PATTERN = Pattern.compile("[\\d.,]+");

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    public EventService(EventsClient eventApi, CurrencyService currencyService) {
        this.eventApi = eventApi;
        this.currencyService = currencyService;
    }

    public CompletableFuture<EventResponse> getFilteredEvents(double budget, String currency, LocalDate dateFrom, LocalDate dateTo) {
        logger.info("Filtering events with budget: {}, currency: {}, dateFrom: {}, dateTo: {}", budget, currency, dateFrom, dateTo);

        if (dateFrom == null && dateTo == null) {
            LocalDate nowLocalDate = LocalDate.now();
            dateFrom = nowLocalDate.minusDays(7);
            dateTo = nowLocalDate;
            logger.info("No date range provided. Defaulting to last 7 days: from {} to {}", dateFrom, dateTo);
        }

        CompletableFuture<List<Event>> eventsFuture = getEvents(dateFrom, dateTo);

        CompletableFuture<Double> convertedBudgetFuture = CompletableFuture.supplyAsync(() -> {
            CurrencyRateResponse currencyRateResponse = currencyService.getCurrencyRate(currency);
            return currencyRateResponse.getRate().doubleValue() * budget;
        }, executorService);

        return eventsFuture.thenCombine(convertedBudgetFuture, (events, convertedBudget) -> {
            if (events.isEmpty()) {
                throw new ServiceException("No events found for the specified date range.");
            }

            logger.info("Converted budget: {}", convertedBudget);
            events.forEach(event -> event.setParsedPrice(parsePrice(event.getPrice(), event.isFree())));
            List<Event> filteredEvents = filterEventsByBudget(events, convertedBudget);

            EventResponse eventResponse = new EventResponse();
            eventResponse.setCount(filteredEvents.size());
            eventResponse.setResults(filteredEvents);
            return eventResponse;
        }).handle((result, ex) -> {
            if (ex != null) {
                logger.error("Error occurred while filtering events: {}", ex.getMessage());
                throw new ServiceException("Failed to filter events", ex);
            }
            return result;
        });
    }

    public CompletableFuture<List<Event>> getEvents(LocalDate dateFrom, LocalDate dateTo) {
        return CompletableFuture.supplyAsync(() -> {
                    Long dateFromEpoch = dateFrom.atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond();
                    Long dateToEpoch = dateTo.atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond();
                    logger.info("Fetching events from API with dates: from {} to {}", dateFromEpoch, dateToEpoch);

                    EventResponse response = eventApi.getEvents(dateFromEpoch, dateToEpoch);
                    logger.info("Received response: {}", response);

                    if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                        logger.error("Received null or empty response from API");
                        throw new ServiceException("No events found for the specified date range.");
                    }

                    return response.getResults().stream().map(result -> {
                        boolean isFree = result.isFree();
                        String price = result.getPrice();
                        Double parsedPrice = parsePrice(price, isFree);

                        logger.info("Event details - ID: {}, Title: {}, IsFree: {}, Price: {}, ParsedPrice: {}",
                                result.getId(), result.getTitle(), isFree, price, parsedPrice);

                        return new Event(result.getId(), result.getTitle(), isFree, price, parsedPrice);
                    }).collect(Collectors.toList());
                }, executorService)
                .exceptionally(ex -> {
                    logger.error("Error fetching events: {}", ex.getMessage());
                    throw new ServiceException("Failed to fetch events", ex);
                });
    }

    private List<Event> filterEventsByBudget(List<Event> events, double budget) {
        logger.info("Total events before filtering: {}", events.size());
        List<Event> filteredEvents = events.stream()
                .filter(event -> event.isFree() || (event.getParsedPrice() != null && event.getParsedPrice() <= budget))
                .collect(Collectors.toList());

        logger.info("Total events after filtering by budget {}: {}", budget, filteredEvents.size());
        return filteredEvents;
    }

    private Double parsePrice(String priceString, boolean isFree) {
        if (priceString == null || priceString.isEmpty()) {
            return isFree ? 0.0 : null;
        }

        priceString = priceString.replaceAll("\\s+", "");
        Matcher matcher = PRICE_PATTERN.matcher(priceString);
        if (matcher.find()) {
            logger.info("Parsed price from string '{}': {}", priceString, matcher.group());
            return Double.parseDouble(matcher.group().replace(",", "."));
        } else {
            logger.warn("No valid price found in string '{}'. Returning null.", priceString);
            return isFree ? 0.0 : null;
        }
    }
}


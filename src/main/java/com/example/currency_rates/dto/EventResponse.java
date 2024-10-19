package com.example.currency_rates.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    @JacksonXmlProperty(localName = "count")
    private int count;

    @JacksonXmlProperty(localName = "next")
    private String next;

    @JacksonXmlProperty(localName = "previous")
    private String previous;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "event")
    private List<Event> results;
}




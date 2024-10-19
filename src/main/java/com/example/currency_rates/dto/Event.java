package com.example.currency_rates.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @JacksonXmlProperty(isAttribute = true)
    private int id;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "isFree")
    private boolean isFree;

    @JacksonXmlProperty(localName = "price")
    private String price;

    private Double parsedPrice;
}


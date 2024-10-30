package com.example.currency_rates.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "kudago")
public class KudaGoConfig {
    private int maxConcurrentRequests;
    private String eUrl;
}
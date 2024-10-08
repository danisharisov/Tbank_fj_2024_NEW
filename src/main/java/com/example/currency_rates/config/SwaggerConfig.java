package com.example.currency_rates.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Currency Service API")
                        .version("v1")
                        .description("API для управления валютами и конвертации"))
                .addServersItem(new Server().url("http://localhost:8080").description("Local Server"));
    }
}
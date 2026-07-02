package com.av.portfolio_simulator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for the Alpha Vantage API client.
 * Uses Spring's RestClient (available since Spring 6.1) as the HTTP client.
 * A base URL is pre-configured so service calls only need to specify query parameters.
 */
@Configuration
public class AlphaVantageConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

}

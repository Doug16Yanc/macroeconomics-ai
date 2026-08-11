package com.example.macroeconomics_ai.infrastructure.bcb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BcbRestClientConfig {

    @Bean
    public RestClient bcbRestClient(@Value("${bcb.api.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept","application/json")
                .build();
    }
}

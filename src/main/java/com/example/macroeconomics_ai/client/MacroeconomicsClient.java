package com.example.macroeconomics_ai.client;

import com.example.macroeconomics_ai.dto.MacroeconomicsObservationsResponse;
import com.example.macroeconomics_ai.dto.MacroeconomicsSeriesInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MacroeconomicsClient {

    private static final int DEFAULT_LIMIT = 100;
    private static final String DEFAULT_UNITS = "lin";

    private final RestClient restClient;
    private final String apiKey;

    public MacroeconomicsClient(
            RestClient fredRestClient,
            @Value("${fred.api.key}") String apiKey) {

        this.restClient = fredRestClient;
        this.apiKey = apiKey;
    }

    public MacroeconomicsObservationsResponse getObservations(
            String seriesId,
            String units,
            String observationStart) {

        return getObservations(
                seriesId,
                units,
                observationStart,
                DEFAULT_LIMIT
        );
    }

    public MacroeconomicsObservationsResponse getObservations(
            String seriesId,
            String units,
            String observationStart,
            int limit) {

        return restClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder
                        .path("/series/observations")
                        .queryParam("series_id", seriesId)
                        .queryParam("api_key", apiKey)
                        .queryParam("file_type", "json")
                        .queryParam("sort_order", "desc")
                        .queryParam("limit", limit)
                        .queryParam(
                                "units",
                                units != null && !units.isBlank()
                                        ? units
                                        : DEFAULT_UNITS
                        );

                if (observationStart != null && !observationStart.isBlank()) {
                    builder.queryParam(
                            "observation_start",
                            observationStart
                    );
                }

                return builder.build();
            })
            .retrieve()
            .body(MacroeconomicsObservationsResponse.class);
    }

    @Cacheable("fredSeriesInfo")
    public MacroeconomicsSeriesInfoResponse getSeriesInfo(String seriesId) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/series")
                        .queryParam("series_id", seriesId)
                        .queryParam("api_key", apiKey)
                        .queryParam("file_type", "json")
                        .build())
                .retrieve()
                .body(MacroeconomicsSeriesInfoResponse.class);
    }
}


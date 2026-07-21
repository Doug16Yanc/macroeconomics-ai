package com.example.fredai.client;

import com.example.fredai.dto.MacroeconomicsObservationsResponse;
import com.example.fredai.dto.MacroeconomicsSeriesInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MacroeconomicsClient {

    private final RestClient restClient;
    private final String apiKey;

    public MacroeconomicsClient(RestClient fredRestClient,
                                @Value("${fred.api.key}") String apiKey) {
        this.restClient = fredRestClient;
        this.apiKey = apiKey;
    }

    public MacroeconomicsObservationsResponse getObservations(String seriesId, String observationStart, int limit, String units) {
        return restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/series/observations")
                            .queryParam("series_id", seriesId)
                            .queryParam("api_key", apiKey)
                            .queryParam("file_type", "json")
                            .queryParam("sort_order", "desc")
                            .queryParam("limit", limit)
                            .queryParam("units", units != null ? units : "lin");
                    if (observationStart != null && !observationStart.isBlank()) {
                        builder.queryParam("observation_start", observationStart);
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
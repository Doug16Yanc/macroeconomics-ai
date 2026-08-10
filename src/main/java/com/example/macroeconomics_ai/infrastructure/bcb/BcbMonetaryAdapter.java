package com.example.macroeconomics_ai.infrastructure.bcb;

import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;
import com.example.macroeconomics_ai.domain.port.MonetaryDataPort;
import com.example.macroeconomics_ai.infrastructure.web.dto.BcbObservation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class BcbMonetaryAdapter implements MonetaryDataPort {

    private static final DateTimeFormatter BCB_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient bcbRestClient;

    public BcbMonetaryAdapter(RestClient bcbRestClient) {
        this.bcbRestClient = bcbRestClient;
    }

    @Override
    public List<MonetaryIndicator> getObservations(String seriesId, LocalDate start) {
        var response = bcbRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bcdata.sgs.{seriesId}/dados")
                        .queryParam("formato", "json")
                        .queryParam("dataInicial", start.format(BCB_DATE_FORMAT))
                        .build(seriesId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<BcbObservation>>() {});

        if (response == null) {
            return List.of();
        }

        return response.stream()
                .map(obs -> new MonetaryIndicator(
                        "BCB",
                        seriesId,
                        LocalDate.parse(obs.date(), BCB_DATE_FORMAT),
                        new BigDecimal(obs.value())
                ))
                .toList();
    }
}
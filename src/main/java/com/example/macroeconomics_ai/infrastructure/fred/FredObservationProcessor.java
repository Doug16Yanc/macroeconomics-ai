package com.example.macroeconomics_ai.infrastructure.fred;

import com.example.macroeconomics_ai.client.MacroeconomicsClient;
import com.example.macroeconomics_ai.domain.model.fred.FredObservation;
import com.example.macroeconomics_ai.domain.model.fred.FredSeriesRequest;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
public class FredObservationProcessor
        implements ItemProcessor<FredSeriesRequest, List<FredObservation>> {

    private final MacroeconomicsClient client;

    public FredObservationProcessor(MacroeconomicsClient client) {
        this.client = client;
    }

    @Override
    public List<FredObservation> process(FredSeriesRequest request) {

        var fetchedAt = Instant.now();

        var response = client.getObservations(
                request.seriesId(),
                request.units(),
                request.observationStart().toString()
        );

        return response.observations()
                .stream()
                .filter(obs -> obs.value() != null)
                .filter(obs -> !obs.value().equals("."))
                .map(obs -> new FredObservation(
                        request.seriesId(),
                        LocalDate.parse(obs.date()),
                        new BigDecimal(obs.value()),
                        fetchedAt
                ))
                .toList();
    }
}


package com.example.macroeconomics_ai.infrastructure.fred;

import com.example.macroeconomics_ai.client.MacroeconomicsClient;
import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;
import com.example.macroeconomics_ai.domain.port.MonetaryDataPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class FredMonetaryAdapter implements MonetaryDataPort {

    private final MacroeconomicsClient macroeconomicsClient;

    public FredMonetaryAdapter(MacroeconomicsClient macroeconomicsClient) {
        this.macroeconomicsClient = macroeconomicsClient;
    }

    @Override
    public List<MonetaryIndicator> getObservations(String seriesId, LocalDate start) {
        var response = macroeconomicsClient.getObservations(
                seriesId,
                "lin",
                start != null ? start.toString() : null
        );

        return response.observations().stream()
                .filter(obs -> obs.value() != null && !obs.value().equals("."))
                .map(obs -> new MonetaryIndicator(
                        "FRED",
                        seriesId,
                        LocalDate.parse(obs.date()),
                        new BigDecimal(obs.value())
                ))
                .toList();
    }
}

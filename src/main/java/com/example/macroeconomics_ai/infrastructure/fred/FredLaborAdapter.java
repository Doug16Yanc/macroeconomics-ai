package com.example.macroeconomics_ai.infrastructure.fred;

import com.example.macroeconomics_ai.client.MacroeconomicsClient;
import com.example.macroeconomics_ai.domain.model.fred.FredLaborIndicator;
import com.example.macroeconomics_ai.domain.port.FredLaborDataPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class FredLaborAdapter implements FredLaborDataPort {

    private final MacroeconomicsClient macroeconomicsClient;

    public FredLaborAdapter(MacroeconomicsClient macroeconomicsClient) {
        this.macroeconomicsClient = macroeconomicsClient;
    }

    @Override
    public List<FredLaborIndicator> getObservations(String indicator, String sector, String region, LocalDate start) {
        if (sector != null || region != null) {
            throw new UnsupportedOperationException(
                    "FRED labor series are not broken down by sector or region; use CagedLaborAdapter for that."
            );
        }

        var response = macroeconomicsClient.getObservations(
                indicator,
                "lin",
                start != null ? start.toString() : null
        );

        return response.observations().stream()
                .filter(obs -> obs.value() != null && !obs.value().equals("."))
                .map(obs -> new FredLaborIndicator(
                        "FRED",
                        null,
                        null,
                        LocalDate.parse(obs.date()),
                        new BigDecimal(obs.value())
                ))
                .toList();
    }
}
package com.example.macroeconomics_ai.domain.model;

import java.time.LocalDate;

public record FredSeriesRequest(
        String seriesId,
        String units,
        LocalDate observationStart
) {
}

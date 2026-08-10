package com.example.macroeconomics_ai.model;

import java.time.LocalDate;

public record FredSeriesRequest(
        String seriesId,
        String units,
        LocalDate observationStart
) {
}

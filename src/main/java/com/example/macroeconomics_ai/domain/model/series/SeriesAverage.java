package com.example.macroeconomics_ai.domain.model.series;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SeriesAverage(
        String seriesId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal average,
        int observationCount
) {
}

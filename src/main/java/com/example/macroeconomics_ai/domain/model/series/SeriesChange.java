package com.example.macroeconomics_ai.domain.model.series;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SeriesChange(
        String seriesId,
        LocalDate fromDate,
        BigDecimal fromValue,
        LocalDate toDate,
        BigDecimal toValue,
        BigDecimal absoluteChange,
        BigDecimal percentageChange
) {
}

package com.example.macroeconomics_ai.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonetaryIndicator(
        String source,
        String seriesId,
        LocalDate date,
        BigDecimal value
) {
}

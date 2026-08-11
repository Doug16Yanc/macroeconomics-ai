package com.example.macroeconomics_ai.domain.model.series;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SeriesTrend(
        String seriesId,
        LocalDate startDate,
        LocalDate endDate,
        Trend trend,
        BigDecimal slopePerDay
) {}
package com.example.macroeconomics_ai.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LaborMarketIndicator(
        String source,
        String sector,
        String region,
        LocalDate date,
        BigDecimal value
) {
}

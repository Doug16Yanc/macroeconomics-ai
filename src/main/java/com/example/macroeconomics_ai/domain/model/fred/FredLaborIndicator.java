package com.example.macroeconomics_ai.domain.model.fred;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FredLaborIndicator(
        String source,
        String sector,
        String region,
        LocalDate date,
        BigDecimal value
) {
}

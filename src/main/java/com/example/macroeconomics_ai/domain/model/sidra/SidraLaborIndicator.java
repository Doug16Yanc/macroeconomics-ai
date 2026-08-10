package com.example.macroeconomics_ai.domain.model.sidra;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SidraLaborIndicator(
        String source,
        String tableId,
        String variableId,
        String variableName,
        String territoryId,
        String territoryName,
        String periodCode,
        String periodName,
        String classificationId,
        String categoryId,
        String categoryName,
        LocalDate date,
        BigDecimal value,
        String unit
) {
}

package com.example.macroeconomics_ai.domain.model.sidra;

import java.time.LocalDate;

public record SidraLaborQuery(
        String tableId,
        String variableId,
        String classificationId,
        String categoryId,
        String region,
        LocalDate start
) {
}
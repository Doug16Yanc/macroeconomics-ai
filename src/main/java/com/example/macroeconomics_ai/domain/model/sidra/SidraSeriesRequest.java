package com.example.macroeconomics_ai.domain.model.sidra;

import java.time.LocalDate;

public record SidraSeriesRequest(
        String tableId,
        String variableId,
        String categoryId,
        LocalDate observationStart
) {
}
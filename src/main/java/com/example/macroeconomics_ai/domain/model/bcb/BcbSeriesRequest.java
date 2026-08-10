package com.example.macroeconomics_ai.domain.model.bcb;

import java.time.LocalDate;

public record BcbSeriesRequest(String seriesId, LocalDate observationStart) {
}
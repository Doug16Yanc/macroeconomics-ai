package com.example.macroeconomics_ai.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FredObservation(
        String seriesId,
        LocalDate date,
        BigDecimal value,
        Instant fetchedAt
) {}
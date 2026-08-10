package com.example.macroeconomics_ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MacroeconomicsObservationsResponse(
        String realtimeStart,
        String realtimeEnd,
        String observationStart,
        String observationEnd,
        int count,
        int offset,
        int limit,
        List<MacroeconomicsObservation> observations
) {
}

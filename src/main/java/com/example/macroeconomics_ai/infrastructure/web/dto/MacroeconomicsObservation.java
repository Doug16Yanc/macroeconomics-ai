package com.example.macroeconomics_ai.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MacroeconomicsObservation(
        String realtimeStart,
        String realtimeEnd,
        String date,
        String value
) {
}

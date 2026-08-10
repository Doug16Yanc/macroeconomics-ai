package com.example.macroeconomics_ai.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BcbObservation(
        @JsonProperty("data") String date,
        @JsonProperty("valor") String value
) {
}
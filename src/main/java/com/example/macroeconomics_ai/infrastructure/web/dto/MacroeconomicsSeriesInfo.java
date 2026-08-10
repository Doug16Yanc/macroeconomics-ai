package com.example.macroeconomics_ai.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MacroeconomicsSeriesInfo(
        String id,
        String title,
        String units,
        @JsonProperty("units_short") String unitsShort,
        String frequency,
        @JsonProperty("frequency_short") String frequencyShort
) { }

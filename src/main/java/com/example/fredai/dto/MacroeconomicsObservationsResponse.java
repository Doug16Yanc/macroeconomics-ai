package com.example.fredai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MacroeconomicsObservationsResponse(int count, List<MacroeconomicsObservation> observations) {
}

package com.example.fredai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MacroeconomicsObservation(String date, String value) {
}

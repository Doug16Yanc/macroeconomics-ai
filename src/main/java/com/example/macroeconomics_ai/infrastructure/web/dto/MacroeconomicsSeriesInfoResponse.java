package com.example.macroeconomics_ai.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MacroeconomicsSeriesInfoResponse(List<MacroeconomicsSeriesInfo> seriess) { }


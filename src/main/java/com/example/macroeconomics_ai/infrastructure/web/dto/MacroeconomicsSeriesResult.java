package com.example.macroeconomics_ai.infrastructure.web.dto;

import java.util.List;

public record MacroeconomicsSeriesResult(
        String seriesId,
        String title,
        String units,
        List<MacroeconomicsObservation> observations
) { }
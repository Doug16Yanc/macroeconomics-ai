package com.example.macroeconomics_ai.dto;

import java.util.List;

public record MacroeconomicsSeriesResult(
        String seriesId,
        String title,
        String units,
        List<MacroeconomicsObservation> observations
) { }
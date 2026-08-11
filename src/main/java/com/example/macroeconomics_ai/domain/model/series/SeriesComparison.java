package com.example.macroeconomics_ai.domain.model.series;

public record SeriesComparison(
        SeriesChange seriesA,
        SeriesChange seriesB,
        String moreVolatileSeriesId
) {
}

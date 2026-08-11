package com.example.macroeconomics_ai.domain.model.sidra;

import com.example.macroeconomics_ai.domain.ActivityStats;

import java.util.List;

public record OccupationSummary(
        String referencePeriod,
        List<ActivityStats> activities
) {
}

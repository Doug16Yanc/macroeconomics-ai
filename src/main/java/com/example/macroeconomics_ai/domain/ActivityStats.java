package com.example.macroeconomics_ai.domain;

import java.math.BigDecimal;

public record ActivityStats(
        String category,
        BigDecimal employedThousands,
        BigDecimal startEmployedThousands,
        BigDecimal percentageChangeInPeriod,
        String periodChangeStart,
        String periodChangeEnd,
        int periodCount,
        int increasingPeriods,
        int decreasingPeriods,
        int stablePeriods,
        BigDecimal startSharePercentage,
        BigDecimal currentSharePercentage,
        BigDecimal shareChangePercentagePoints,
        BigDecimal trendSlope
) {
}


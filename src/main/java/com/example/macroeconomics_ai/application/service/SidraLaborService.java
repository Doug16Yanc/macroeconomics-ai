package com.example.macroeconomics_ai.application.service;

import com.example.macroeconomics_ai.domain.ActivityStats;
import com.example.macroeconomics_ai.domain.model.sidra.*;
import com.example.macroeconomics_ai.infrastructure.persistence.repository.SidraObservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SidraLaborService {

    private static final String TABLE_ID = "5434";
    private static final String EMPLOYED_THOUSANDS_VARIABLE = "4090";
    private static final String SHARE_PERCENTAGE_VARIABLE = "4108";

    private final SidraObservationRepository repository;

    public SidraLaborService(SidraObservationRepository repository) {
        this.repository = repository;
    }

    public OccupationSummary occupationByActivity(int quartersBack) {
        List<SidraLaborIndicator> indicators = repository.findByTableAndVariables(
                TABLE_ID, List.of(EMPLOYED_THOUSANDS_VARIABLE, SHARE_PERCENTAGE_VARIABLE));

        LocalDate mostRecent = indicators.stream()
                .map(SidraLaborIndicator::date)
                .max(Comparator.naturalOrder())
                .orElse(LocalDate.now());

        LocalDate cutoff = mostRecent.minusMonths((quartersBack - 1) * 3L);

        var employed = filterByVariable(indicators, EMPLOYED_THOUSANDS_VARIABLE, cutoff);
        var share = filterByVariable(indicators, SHARE_PERCENTAGE_VARIABLE, cutoff);
        Map<String, List<SidraLaborIndicator>> byCategory = employed.stream()
                .collect(Collectors.groupingBy(SidraLaborIndicator::categoryName));

        List<ActivityStats> stats = byCategory.entrySet().stream()
                .map(e -> computeStats(e.getKey(), e.getValue(), share))
                .sorted(Comparator.comparing(ActivityStats::currentSharePercentage,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        String referencePeriod = employed.stream()
                .max(Comparator.comparing(SidraLaborIndicator::date))
                .map(SidraLaborIndicator::periodName)
                .orElse("unknown");

        return new OccupationSummary(referencePeriod, stats);
    }

    private List<SidraLaborIndicator> filterByVariable(List<SidraLaborIndicator> indicators, String variableId, LocalDate cutoff) {
            return indicators.stream()
                    .filter(i -> variableId.equals(i.variableId()))
                    .filter(i -> i.date() != null && !i.date().isBefore(cutoff))
                    .toList();
    }

    private ActivityStats computeStats(String category, List<SidraLaborIndicator> series, List<SidraLaborIndicator> shareSeries) {

        var ordered = series.stream()
                .filter(i -> i.value() != null)
                .sorted(Comparator.comparing(SidraLaborIndicator::date))
                .toList();

        if (ordered.isEmpty()) {
            return new ActivityStats(
                    category,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    0,
                    0,
                    0,
                    null,
                    null,
                    null,
                    null
            );
        }

        var first = ordered.getFirst();
        var last = ordered.getLast();

        var periodChanges = countPeriodChanges(ordered);

        var startShare = shareAtPeriod(category, first.periodCode(), shareSeries);
        var currentShare = shareAtPeriod(category, last.periodCode(), shareSeries);

        return new ActivityStats(
                category,
                last.value(),
                first.value(),
                percentageChange(first.value(), last.value()),
                first.periodName(),
                last.periodName(),
                ordered.size(),
                periodChanges.increasing(),
                periodChanges.decreasing(),
                periodChanges.stable(),
                startShare,
                currentShare,
                percentagePointChange(startShare, currentShare),
                linearTrend(ordered)
        );
    }

    private PeriodChanges countPeriodChanges(List<SidraLaborIndicator> ordered) {

        int increasing = 0;
        int decreasing = 0;
        int stable = 0;

        for (int i = 1; i < ordered.size(); i++) {
            int comparison = ordered.get(i).value()
                    .compareTo(ordered.get(i - 1).value());

            if (comparison > 0) {
                increasing++;
            } else if (comparison < 0) {
                decreasing++;
            } else {
                stable++;
            }
        }

        return new PeriodChanges(increasing, decreasing, stable);

    }

    private BigDecimal shareAtPeriod(String category, String periodCode, List<SidraLaborIndicator> shareSeries) {

        return shareSeries.stream()
                .filter(i -> category.equals(i.categoryName()))
                .filter(i -> i.value() != null)
                .filter(i -> periodCode.equals(i.periodCode()))
                .map(SidraLaborIndicator::value)
                .findFirst()
                .orElse(null);

    }


    private BigDecimal percentageChange(BigDecimal start, BigDecimal end) {
        if (start == null || end == null || start.compareTo(BigDecimal.ZERO) == 0) return null;
        return end.subtract(start)
                .divide(start, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentagePointChange(
            BigDecimal start,
            BigDecimal end) {

        if (start == null || end == null) {
            return null;
        }

        return end.subtract(start)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal linearTrend(List<SidraLaborIndicator> series) {
        int n = series.size();
        if (n < 2) return BigDecimal.ZERO;
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            double y = series.get(i).value().doubleValue();
            sumX += i; sumY += y; sumXY += i * y; sumXX += (double) i * i;
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        return BigDecimal.valueOf(slope).setScale(3, RoundingMode.HALF_UP);
    }
}
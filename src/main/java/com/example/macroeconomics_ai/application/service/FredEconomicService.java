package com.example.macroeconomics_ai.application.service;

import com.example.macroeconomics_ai.domain.model.fred.*;
import com.example.macroeconomics_ai.domain.model.series.*;
import com.example.macroeconomics_ai.infrastructure.persistence.repository.FredObservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class FredEconomicService {

    private static final int SCALE = 6;
    private static final BigDecimal TREND_THRESHOLD_PERCENT = new BigDecimal("1.0");

    private final FredObservationRepository repository;

    public FredEconomicService(FredObservationRepository repository) {
        this.repository = repository;
    }

    public List<FredObservation> findObservations(String seriesId, LocalDate startDate, LocalDate endDate) {
        var observations = repository.findObservations(seriesId, startDate, endDate);
        if (observations.isEmpty()) {
            throw new IllegalStateException(
                    "Nenhuma observação encontrada para a série %s entre %s e %s"
                            .formatted(seriesId, startDate, endDate));
        }
        return observations;
    }

    public Optional<FredObservation> findLatest(String seriesId) {
        return repository.findLatest(seriesId);
    }

    public SeriesChange calculateChange(String seriesId, LocalDate startDate, LocalDate endDate) {
        var observations = findObservations(seriesId, startDate, endDate);

        var first = observations.getFirst();
        var last = observations.getLast();

        var absoluteChange = last.value().subtract(first.value());
        var percentageChange = calculatePercentageChange(first.value(), absoluteChange);

        return new SeriesChange(
                seriesId,
                first.date(), first.value(),
                last.date(), last.value(),
                absoluteChange, percentageChange
        );
    }

    public SeriesAverage calculateAverage(String seriesId, LocalDate startDate, LocalDate endDate) {
        var observations = findObservations(seriesId, startDate, endDate);

        var sum = observations.stream()
                .map(FredObservation::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var average = sum.divide(BigDecimal.valueOf(observations.size()), SCALE, RoundingMode.HALF_UP);

        return new SeriesAverage(seriesId, startDate, endDate, average, observations.size());
    }

    public SeriesTrend calculateTrend(String seriesId, LocalDate startDate, LocalDate endDate) {
        var observations = findObservations(seriesId, startDate, endDate);

        if (observations.size() < 2) {
            return new SeriesTrend(seriesId, startDate, endDate, Trend.STABLE, BigDecimal.ZERO);
        }

        var baseDate = observations.getFirst().date();
        double[] xs = observations.stream()
                .mapToDouble(o -> ChronoUnit.DAYS.between(baseDate, o.date()))
                .toArray();
        double[] ys = observations.stream()
                .mapToDouble(o -> o.value().doubleValue())
                .toArray();

        double xMean = average(xs);
        double yMean = average(ys);

        double numerator = 0, denominator = 0;
        for (int i = 0; i < xs.length; i++) {
            numerator += (xs[i] - xMean) * (ys[i] - yMean);
            denominator += (xs[i] - xMean) * (xs[i] - xMean);
        }

        double slopePerDay = denominator == 0 ? 0 : numerator / denominator;
        double totalDays = xs[xs.length - 1] - xs[0];
        double projectedChange = slopePerDay * totalDays;
        double changePercentOfMean = yMean == 0 ? 0 : Math.abs(projectedChange / yMean) * 100;

        Trend trend;
        if (changePercentOfMean < TREND_THRESHOLD_PERCENT.doubleValue()) {
            trend = Trend.STABLE;
        } else {
            trend = projectedChange > 0 ? Trend.RISING : Trend.FALLING;
        }

        return new SeriesTrend(seriesId, startDate, endDate, trend, BigDecimal.valueOf(slopePerDay).setScale(SCALE, RoundingMode.HALF_UP));
    }

    public SeriesComparison compareSeries(String seriesIdA, String seriesIdB, LocalDate startDate, LocalDate endDate) {
        var changeA = calculateChange(seriesIdA, startDate, endDate);
        var changeB = calculateChange(seriesIdB, startDate, endDate);

        var moreVolatile = changeA.percentageChange().abs().compareTo(changeB.percentageChange().abs()) >= 0
                ? seriesIdA
                : seriesIdB;

        return new SeriesComparison(changeA, changeB, moreVolatile);
    }

    private BigDecimal calculatePercentageChange(BigDecimal fromValue, BigDecimal absoluteChange) {
        if (fromValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Não é possível calcular variação percentual: valor inicial da série é zero");
        }
        return absoluteChange
                .divide(fromValue.abs(), SCALE, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private double average(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }
}
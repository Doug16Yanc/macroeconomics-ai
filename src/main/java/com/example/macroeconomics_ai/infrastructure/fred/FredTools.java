package com.example.macroeconomics_ai.infrastructure.fred;

import com.example.macroeconomics_ai.application.service.FredEconomicService;
import com.example.macroeconomics_ai.client.MacroeconomicsClient;
import com.example.macroeconomics_ai.domain.model.series.SeriesAverage;
import com.example.macroeconomics_ai.domain.model.series.SeriesChange;
import com.example.macroeconomics_ai.domain.model.series.SeriesTrend;
import com.example.macroeconomics_ai.domain.port.FredLaborDataPort;
import com.example.macroeconomics_ai.infrastructure.web.dto.MacroeconomicsObservation;
import com.example.macroeconomics_ai.infrastructure.web.dto.MacroeconomicsSeriesResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

@Component
public class FredTools {

    private final MacroeconomicsClient macroeconomicsClient;
    private final FredEconomicService fredEconomicService;
    private final FredLaborDataPort fredLaborAdapter;
    private final ObjectMapper objectMapper;

    public FredTools(
            MacroeconomicsClient macroeconomicsClient,
            FredEconomicService fredEconomicService,
            @Qualifier("fredLaborAdapter") FredLaborDataPort fredLaborAdapter,
            ObjectMapper objectMapper) {

        this.macroeconomicsClient = macroeconomicsClient;
        this.fredEconomicService = fredEconomicService;
        this.fredLaborAdapter = fredLaborAdapter;
        this.objectMapper = objectMapper;
    }

    @Tool(description = """
            Returns a full analysis of a FRED economic series over a lookback window:
            latest value, absolute and percentage change, average, and trend direction.
            Use this instead of fetching raw observations when the user wants context,
            not just numbers.
            """)
    public String getSeriesAnalysis(
            @ToolParam(description = "Official FRED series ID, e.g. CPIAUCSL, UNRATE, FEDFUNDS, SP500, DGS10, M2SL")
            String seriesId,
            @ToolParam(description = "Number of recent months to look back")
            int monthsBack) {

        var start = LocalDate.now().minusMonths(monthsBack);
        var end = LocalDate.now();

        var change = fredEconomicService.calculateChange(seriesId, start, end);
        var average = fredEconomicService.calculateAverage(seriesId, start, end);
        var trend = fredEconomicService.calculateTrend(seriesId, start, end);
        var title = resolveTitle(seriesId);

        record SeriesAnalysisResult(
                String seriesId, String title,
                SeriesChange change, SeriesAverage average, SeriesTrend trend
        ) {}

        return serialize(new SeriesAnalysisResult(seriesId, title, change, average, trend));
    }

    @Tool(description = """
            Compares two FRED series over the same lookback window, returning
            the change of each and which one moved more (more volatile).
            Useful for questions like inflation vs interest rates, or S&P 500 vs M2.
            """)
    public String compareTwoSeries(
            @ToolParam(description = "First FRED series ID") String seriesIdA,
            @ToolParam(description = "Second FRED series ID") String seriesIdB,
            @ToolParam(description = "Number of recent months to look back") int monthsBack) {

        var start = LocalDate.now().minusMonths(monthsBack);
        var end = LocalDate.now();

        return serialize(fredEconomicService.compareSeries(seriesIdA, seriesIdB, start, end));
    }

    @Tool(description = """
            Returns raw observations for any FRED series over a lookback window,
            including title and unit. Use only when the user explicitly wants
            the raw data points, not an analysis.
            """)
    public MacroeconomicsSeriesResult fredCustomSerie(
            @ToolParam(description = "Official FRED series ID, e.g., SP500, DGS10, PAYEMS")
            String seriesId,
            @ToolParam(description = "Number of recent months to look back")
            int monthsBack) {

        var start = LocalDate.now().minusMonths(monthsBack);
        var end = LocalDate.now();

        var observations = fredEconomicService.findObservations(seriesId, start, end);
        var info = macroeconomicsClient.getSeriesInfo(seriesId).seriess().stream().findFirst().orElse(null);

        String title = info != null ? info.title() : seriesId;
        String units = info != null ? info.unitsShort() : "unknown";

        var rawObservations = observations.stream()
                .map(obs -> new MacroeconomicsObservation(null, null, obs.date().toString(), obs.value().toString()))
                .toList();

        return new MacroeconomicsSeriesResult(seriesId, title, units, rawObservations);
    }

    @Tool(description = """
            Get labor market data from FRED.
            Supported series: UNRATE, PAYEMS, ICSA, JTSJOL.
            """)
    public String getLaborMarketSeries(
            @ToolParam(description = "FRED series ID: UNRATE, PAYEMS, ICSA, JTSJOL") String seriesId,
            @ToolParam(description = "Number of months to look back") int monthsBack) {

        var start = LocalDate.now().minusMonths(monthsBack);
        var observations = fredLaborAdapter.getObservations(seriesId, null, null, start);
        return serialize(observations);
    }

    private String resolveTitle(String seriesId) {
        var info = macroeconomicsClient.getSeriesInfo(seriesId).seriess().stream().findFirst().orElse(null);
        return info != null ? info.title() : seriesId;
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize FRED data", e);
        }
    }
}

package com.example.macroeconomics_ai.infrastructure.fred;

import com.example.macroeconomics_ai.client.MacroeconomicsClient;
import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;
import com.example.macroeconomics_ai.domain.port.FredLaborDataPort;
import com.example.macroeconomics_ai.domain.port.MonetaryDataPort;
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
    private final MonetaryDataPort fredMonetaryAdapter;
    private final FredLaborDataPort fredLaborAdapter;
    private final ObjectMapper objectMapper;

    public FredTools(
            MacroeconomicsClient macroeconomicsClient,
            @Qualifier("fredMonetaryAdapter") MonetaryDataPort fredMonetaryAdapter,
            @Qualifier("fredLaborAdapter") FredLaborDataPort fredLaborAdapter,
            ObjectMapper objectMapper) {

        this.macroeconomicsClient = macroeconomicsClient;
        this.fredMonetaryAdapter = fredMonetaryAdapter;
        this.fredLaborAdapter = fredLaborAdapter;
        this.objectMapper = objectMapper;
    }

    @Tool(description = """
            Returns the most recent quarterly U.S. GDP (Gross Domestic Product)
            values, including title and unit, in billions of dollars.
            """)
    public MacroeconomicsSeriesResult americanPib(
            @ToolParam(description = "Number of recent quarters to look back, roughly")
            int monthsBack) {

        return buildResult("GDP", monthsBack, null);
    }

    @Tool(description = """
            Returns the most recent monthly U.S. year-over-year inflation rate
            (CPI percentage change vs. the same month one year earlier),
            including title and unit, as a percentage.
            """)
    public MacroeconomicsSeriesResult americanInflation(
            @ToolParam(description = "Number of recent months to look back")
            int monthsBack) {

        return buildResult("CPIAUCSL", monthsBack, "Percent change from year ago");
    }

    @Tool(description = """
            Returns the most recent monthly U.S. unemployment rate,
            including title and unit, in percentage.
            """)
    public MacroeconomicsSeriesResult americanUnemploymentRate(
            @ToolParam(description = "Number of recent months to look back")
            int monthsBack) {

        return buildResult("UNRATE", monthsBack, null);
    }

    @Tool(description = """
            Returns the most recent monthly U.S. Federal Funds Rate set by
            the FED, including title and unit, in percentage.
            """)
    public MacroeconomicsSeriesResult fedInterestRate(
            @ToolParam(description = "Number of recent months to look back")
            int monthsBack) {

        return buildResult("FEDFUNDS", monthsBack, null);
    }

    @Tool(description = """
            Returns observations for any FRED economic series using its
            official series_id (e.g., SP500, DGS10, PAYEMS, M2SL),
            including the series title and unit. Returns raw level values.
            """)
    public MacroeconomicsSeriesResult fredCustomSerie(
            @ToolParam(description = "Official FRED series ID, e.g., SP500, DGS10, PAYEMS")
            String seriesId,

            @ToolParam(description = "Number of recent months to look back")
            int monthsBack) {

        return buildResult(seriesId, monthsBack, null);
    }

    @Tool(description = """
            Get labor market data from FRED.

            Supported series:
            UNRATE = unemployment rate,
            PAYEMS = total nonfarm payroll employment,
            ICSA = initial unemployment insurance claims,
            JTSJOL = job openings.
            """)
    public String getLaborMarketSeries(
            @ToolParam(description = "FRED series ID: UNRATE, PAYEMS, ICSA, JTSJOL")
            String seriesId,

            @ToolParam(description = "Number of months to look back")
            int monthsBack) {

        var start = LocalDate.now().minusMonths(monthsBack);
        var observations = fredLaborAdapter.getObservations(seriesId, null, null, start);

        return serialize(observations);
    }

    private MacroeconomicsSeriesResult buildResult(String seriesId, int monthsBack, String unitsLabelOverride) {
        var start = LocalDate.now().minusMonths(monthsBack);
        List<MonetaryIndicator> observations = fredMonetaryAdapter.getObservations(seriesId, start);

        var info = macroeconomicsClient
                .getSeriesInfo(seriesId)
                .seriess()
                .stream()
                .findFirst()
                .orElse(null);

        String title = info != null ? info.title() : seriesId;
        String resolvedUnits = unitsLabelOverride != null
                ? unitsLabelOverride
                : info != null ? info.unitsShort() : "unknown";

        var rawObservations = observations.stream()
                .map(obs -> new com.example.macroeconomics_ai.infrastructure.web.dto.MacroeconomicsObservation(
                        null, null, obs.date().toString(), obs.value().toString()))
                .toList();

        return new MacroeconomicsSeriesResult(seriesId, title, resolvedUnits, rawObservations);
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize FRED observations", e);
        }
    }
}

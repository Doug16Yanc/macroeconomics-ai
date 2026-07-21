package com.example.fredai.tool;

import com.example.fredai.client.MacroeconomicsClient;
import com.example.fredai.dto.MacroeconomicsSeriesResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class MacroeconomicsTools {

    private final MacroeconomicsClient macroeconomicsClient;

    public MacroeconomicsTools(MacroeconomicsClient macroeconomicsClient) {
        this.macroeconomicsClient = macroeconomicsClient;
    }

    @Tool(description = "Returns the most recent quarterly U.S. GDP (Gross Domestic Product) values, " +
            "including title and unit, in billions of dollars.")
    public MacroeconomicsSeriesResult americanPib(
            @ToolParam(description = "Number of recent observations/quarters to return") int quantity) {
        return buildResult("GDP", quantity, "lin", null);
    }

    @Tool(description = "Returns the most recent monthly U.S. year-over-year inflation rate " +
            "(CPI percentage change vs. the same month one year earlier), including title and unit, as a percentage.")
    public MacroeconomicsSeriesResult americanInflation(
            @ToolParam(description = "Number of recent observations/months to return") int quantity) {
        return buildResult("CPIAUCSL", quantity, "pc1", "Percent change from year ago");
    }

    @Tool(description = "Returns the most recent monthly U.S. unemployment rate, including title and unit, in percentage.")
    public MacroeconomicsSeriesResult americanUnemploymentRate(
            @ToolParam(description = "Number of recent observations/months to return") int quantity) {
        return buildResult("UNRATE", quantity, "lin", null);
    }

    @Tool(description = "Returns the most recent monthly U.S. Federal Funds Rate set by the FED, " +
            "including title and unit, in percentage.")
    public MacroeconomicsSeriesResult fedInterestRate(
            @ToolParam(description = "Number of recent observations/months to return") int quantity) {
        return buildResult("FEDFUNDS", quantity, "lin", null);
    }

    @Tool(description = "Returns observations for any FRED economic series using its official series_id " +
            "(e.g., SP500, DGS10, PAYEMS, M2SL), including the series title and unit. Returns raw level values.")
    public MacroeconomicsSeriesResult fredCustomSerie(
            @ToolParam(description = "Official FRED series ID, e.g., SP500, DGS10, PAYEMS") String seriesId,
            @ToolParam(description = "Number of recent observations to return") int quantity) {
        return buildResult(seriesId, quantity, "lin", null);
    }

    private MacroeconomicsSeriesResult buildResult(String seriesId, int quantity, String units, String unitsLabelOverride) {
        var observations = macroeconomicsClient.getObservations(seriesId, null, quantity, units).observations();
        var info = macroeconomicsClient.getSeriesInfo(seriesId).seriess().stream()
                .findFirst()
                .orElse(null);

        String title = info != null ? info.title() : seriesId;
        String resolvedUnits = unitsLabelOverride != null
                ? unitsLabelOverride
                : (info != null ? info.unitsShort() : "unknown");

        return new MacroeconomicsSeriesResult(seriesId, title, resolvedUnits, observations);
    }
}
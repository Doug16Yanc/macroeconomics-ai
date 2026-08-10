package com.example.macroeconomics_ai.infrastructure.ai;

import com.example.macroeconomics_ai.client.MacroeconomicsClient;
import com.example.macroeconomics_ai.infrastructure.web.dto.MacroeconomicsSeriesResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

@Component
public class MacroeconomicsTools {

    private final MacroeconomicsClient macroeconomicsClient;
    private final ObjectMapper objectMapper;

    public MacroeconomicsTools(
            MacroeconomicsClient macroeconomicsClient,
            ObjectMapper objectMapper) {

        this.macroeconomicsClient = macroeconomicsClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = """
            Returns the most recent quarterly U.S. GDP (Gross Domestic Product)
            values, including title and unit, in billions of dollars.
            """)
    public MacroeconomicsSeriesResult americanPib(
            @ToolParam(description = "Number of recent observations/quarters to return")
            int quantity) {

        return buildResult(
                "GDP",
                quantity,
                "lin",
                null
        );
    }

    @Tool(description = """
            Returns the most recent monthly U.S. year-over-year inflation rate
            (CPI percentage change vs. the same month one year earlier),
            including title and unit, as a percentage.
            """)
    public MacroeconomicsSeriesResult americanInflation(
            @ToolParam(description = "Number of recent observations/months to return")
            int quantity) {

        return buildResult(
                "CPIAUCSL",
                quantity,
                "pc1",
                "Percent change from year ago"
        );
    }

    @Tool(description = """
            Returns the most recent monthly U.S. unemployment rate,
            including title and unit, in percentage.
            """)
    public MacroeconomicsSeriesResult americanUnemploymentRate(
            @ToolParam(description = "Number of recent observations/months to return")
            int quantity) {

        return buildResult(
                "UNRATE",
                quantity,
                "lin",
                null
        );
    }

    @Tool(description = """
            Returns the most recent monthly U.S. Federal Funds Rate set by
            the FED, including title and unit, in percentage.
            """)
    public MacroeconomicsSeriesResult fedInterestRate(
            @ToolParam(description = "Number of recent observations/months to return")
            int quantity) {

        return buildResult(
                "FEDFUNDS",
                quantity,
                "lin",
                null
        );
    }

    @Tool(description = """
            Returns observations for any FRED economic series using its
            official series_id (e.g., SP500, DGS10, PAYEMS, M2SL),
            including the series title and unit. Returns raw level values.
            """)
    public MacroeconomicsSeriesResult fredCustomSerie(
            @ToolParam(description = "Official FRED series ID, e.g., SP500, DGS10, PAYEMS")
            String seriesId,

            @ToolParam(description = "Number of recent observations to return")
            int quantity) {

        return buildResult(
                seriesId,
                quantity,
                "lin",
                null
        );
    }

    @Tool(description = """
            Get labor market data from FRED.

            Supported series:
            UNRATE = unemployment rate,
            PAYEMS = total nonfarm payroll employment,
            ICSA = initial unemployment insurance claims,
            JTSJOL = job openings.

            Returns observations from the requested number of months
            before the current date.
            """)
    public String getLaborMarketSeries(
            @ToolParam(description = "FRED series ID: UNRATE, PAYEMS, ICSA, JTSJOL")
            String seriesId,

            @ToolParam(description = "Number of months to look back")
            int monthsBack) {

        var observationStart = LocalDate.now()
                .minusMonths(monthsBack)
                .toString();

        var response = macroeconomicsClient.getObservations(
                seriesId,
                "lin",
                observationStart
        );

        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize FRED observations",
                    e
            );
        }
    }

    private MacroeconomicsSeriesResult buildResult(
            String seriesId,
            int quantity,
            String units,
            String unitsLabelOverride) {

        var response = macroeconomicsClient.getObservations(
                seriesId,
                units,
                null,
                quantity
        );

        var observations = response.observations();

        var info = macroeconomicsClient
                .getSeriesInfo(seriesId)
                .seriess()
                .stream()
                .findFirst()
                .orElse(null);

        String title = info != null
                ? info.title()
                : seriesId;

        String resolvedUnits = unitsLabelOverride != null
                ? unitsLabelOverride
                : info != null
                ? info.unitsShort()
                : "unknown";

        return new MacroeconomicsSeriesResult(
                seriesId,
                title,
                resolvedUnits,
                observations
        );
    }
}


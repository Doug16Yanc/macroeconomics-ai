package com.example.macroeconomics_ai.infrastructure.bcb;

import com.example.macroeconomics_ai.application.service.BcbMonetaryService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class BcbTools {

    private final BcbMonetaryService bcbMonetaryService;
    private final ObjectMapper objectMapper;

    private final String selicSeriesId;
    private final String ipcaSeriesId;

    public BcbTools(
            BcbMonetaryService bcbMonetaryService,
            ObjectMapper objectMapper,
            @Value("${bcb.series.selic}") String selicSeriesId,
            @Value("${bcb.series.ipca}") String ipcaSeriesId) {

        this.bcbMonetaryService = bcbMonetaryService;
        this.objectMapper = objectMapper;
        this.selicSeriesId = selicSeriesId;
        this.ipcaSeriesId = ipcaSeriesId;
    }

    @Tool(description = """
            Returns the Brazilian Meta Selic, the target interest rate
            defined by the Central Bank of Brazil's Monetary Policy Committee (COPOM).
            Values are expressed as percentage per year (% p.a.).
            Use this tool when the user asks for the Selic rate,
            Selic target, or Brazilian base interest rate.
            """)
    public String brazilSelicRate(
            @ToolParam(description = "Number of months to look back") int monthsBack) {
        return call(selicSeriesId, monthsBack);
    }

    @Tool(description = """
            Returns the Brazilian IPCA inflation rate,
            expressed as monthly percentage change.
            """)
    public String brazilInflation(
            @ToolParam(description = "Number of months to look back") int monthsBack) {
        return call(ipcaSeriesId, monthsBack);
    }

    @Tool(description = """
            Returns any Brazilian BCB/SGS series by its official numeric code,
            already persisted and filtered up to the current date.
            Use this tool when a specific BCB/SGS series is requested.
            """)
    public String bcbCustomSerie(
            @ToolParam(description = "Official BCB/SGS series code") String seriesId,
            @ToolParam(description = "Number of months to look back") int monthsBack) {
        return call(seriesId, monthsBack);
    }

    private String call(String seriesId, int monthsBack) {
        var observations = bcbMonetaryService.getSeries(seriesId, monthsBack);
        try {
            return objectMapper.writeValueAsString(observations);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize BCB observations", e);
        }
    }
}
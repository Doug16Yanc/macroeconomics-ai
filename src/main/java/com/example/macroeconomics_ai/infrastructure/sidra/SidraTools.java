package com.example.macroeconomics_ai.infrastructure.sidra;

import com.example.macroeconomics_ai.application.service.SidraLaborService;
import com.example.macroeconomics_ai.domain.model.sidra.OccupationSummary;
import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborQuery;
import com.example.macroeconomics_ai.domain.port.SidraLaborDataPort;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

@Component
public class SidraTools {

    private final SidraLaborService sidraLaborService;

    public SidraTools(SidraLaborService sidraLaborService) {
        this.sidraLaborService = sidraLaborService;
    }

    @Tool(description = """
            Returns Brazilian employment distribution by economic activity group,
            from IBGE's PNAD Contínua (SIDRA table 5434). Percentage change,
            employment trend, period-by-period movements, sectoral share at
            the beginning and end of the analysis window, and share change in percentage points
            are already computed. Do not recalculate them. Use the provided metrics directly
            to distinguish changes in employment from changes in sectoral composition.
            """)
    public String brazilEmploymentByActivity(
            @ToolParam(description = "Number of quarters to look back") int quartersBack) {
        return format(sidraLaborService.occupationByActivity(quartersBack));
    }

    private String format(OccupationSummary summary) {

        StringBuilder sb = new StringBuilder(
                "Reference period: " + summary.referencePeriod() + "\n\n"
        );

        for (var a : summary.activities()) {
            sb.append("""
          - %s:
          emprego atual: %s mil pessoas
          emprego inicial: %s mil pessoas
          participação inicial: %s%%
          participação atual: %s%%
          mudança na participação: %s pontos percentuais
          variação no período (%s a %s): %s%%
          observações disponíveis: %d
          períodos de crescimento: %d
          períodos de queda: %d
          períodos estáveis: %d
          tendência linear: %s mil pessoas/trimestre
        """.formatted(
                    a.category(),
                    a.employedThousands(),
                    a.startEmployedThousands(),
                    a.startSharePercentage(),
                    a.currentSharePercentage(),
                    a.shareChangePercentagePoints(),
                    a.periodChangeStart(),
                    a.periodChangeEnd(),
                    a.percentageChangeInPeriod(),
                    a.periodCount(),
                    a.increasingPeriods(),
                    a.decreasingPeriods(),
                    a.stablePeriods(),
                    a.trendSlope()
            ));
        }

        return sb.toString();
    }

}
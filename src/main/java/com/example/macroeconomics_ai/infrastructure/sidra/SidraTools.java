package com.example.macroeconomics_ai.infrastructure.sidra;

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

    private final SidraLaborDataPort sidraLaborDataPort;
    private final ObjectMapper objectMapper;

    public SidraTools(
            @Qualifier("sidraLaborAdapter")
            SidraLaborDataPort sidraLaborDataPort,
            ObjectMapper objectMapper) {

        this.sidraLaborDataPort = sidraLaborDataPort;
        this.objectMapper = objectMapper;
    }

    @Tool(description = """
            Returns Brazilian employment data from IBGE's PNAD Contínua,
            using SIDRA table 5434.

            The data represents people aged 14 or older who were employed
            during the reference week.

            The table can be filtered by activity group and territory.
            """)
    public String brazilEmployment(
            @ToolParam(description = "Number of quarters to look back")
            int quartersBack) {

        var start = LocalDate.now().minusMonths(quartersBack * 3L);

        var query = new SidraLaborQuery(
                "5434",
                "4090",
                "888",
                "47946",
                null,
                start
        );

        var observations = sidraLaborDataPort.getObservations(query);

        try {
            return objectMapper.writeValueAsString(observations);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize SIDRA observations", e);
        }
    }
}
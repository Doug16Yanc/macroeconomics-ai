package com.example.macroeconomics_ai.infrastructure.sidra;

import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborIndicator;
import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborQuery;
import com.example.macroeconomics_ai.domain.port.SidraLaborDataPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class SidraLaborAdapter implements SidraLaborDataPort {

    private final RestClient sidraRestClient;

    public SidraLaborAdapter(RestClient sidraRestClient) {
        this.sidraRestClient = sidraRestClient;
    }

    @Override
    public List<SidraLaborIndicator> getObservations(SidraLaborQuery query) {

        String location = buildLocation(query.region());

        String uri = String.format(
                "/t/%s/%s/v/%s/p/all/c%s/%s",
                query.tableId(),
                location,
                query.variableId(),
                query.classificationId(),
                query.categoryId()
        );

        List<Map<String, String>> rows = sidraRestClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (rows == null || rows.size() <= 1) {
            return List.of();
        }

        return rows.stream()
                .skip(1)
                .map(row -> mapRow(row, query))
                .filter(indicator -> query.start() == null
                        || !indicator.date().isBefore(query.start()))
                .toList();
    }

    private SidraLaborIndicator mapRow(
            Map<String, String> row,
            SidraLaborQuery query
    ) {
        String periodCode = row.get("D3C");

        return new SidraLaborIndicator(
                "SIDRA",
                query.tableId(),
                row.get("D2C"),
                row.get("D2N"),
                row.get("D1C"),
                row.get("D1N"),
                periodCode,
                row.get("D3N"),
                "888",
                row.get("D4C"),
                row.get("D4N"),
                parseQuarterPeriod(periodCode),
                new BigDecimal(row.get("V")),
                row.get("MN")
        );
    }
    private LocalDate parseQuarterPeriod(String periodCode) {

        int year = Integer.parseInt(periodCode.substring(0, 4));
        int quarter = Integer.parseInt(periodCode.substring(4));

        int month = ((quarter - 1) * 3) + 1;

        return LocalDate.of(year, month, 1);
    }

    private String buildLocation(String region) {
        if (region == null || region.isBlank()) {
            return "n1/1"; // Brasil
        }

        return "n3/" + region; // UF
    }
}
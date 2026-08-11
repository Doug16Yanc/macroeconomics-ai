package com.example.macroeconomics_ai.infrastructure.persistence.repository;

import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Repository
public class SidraObservationRepository {

    private static final String SELECT_SQL = """
        SELECT table_id, variable_id, variable_name, territory_id, territory_name,
               period_code, period_name, classification_id, category_id, category_name,
               value, unit
        FROM sidra_observations
        WHERE table_id = ?
          AND variable_id IN (:variableIds)
        ORDER BY period_code
        """;

    private final JdbcTemplate jdbcTemplate;

    public SidraObservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SidraLaborIndicator> findByTableAndVariables(String tableId, List<String> variableIds) {
        String placeholders = String.join(",", variableIds.stream().map(v -> "?").toList());
        String sql = SELECT_SQL.replace(":variableIds", placeholders);

        Object[] params = new Object[1 + variableIds.size()];
        params[0] = tableId;
        for (int i = 0; i < variableIds.size(); i++) {
            params[i + 1] = variableIds.get(i);
        }

        return jdbcTemplate.query(sql, rowMapper(), params);
    }

    private RowMapper<SidraLaborIndicator> rowMapper() {
        return (rs, rowNum) -> {
            String periodCode = rs.getString("period_code");
            BigDecimal value = rs.getBigDecimal("value");

            return new SidraLaborIndicator(
                    "SIDRA",
                    rs.getString("table_id"),
                    rs.getString("variable_id"),
                    rs.getString("variable_name"),
                    rs.getString("territory_id"),
                    rs.getString("territory_name"),
                    periodCode,
                    rs.getString("period_name"),
                    rs.getString("classification_id"),
                    rs.getString("category_id"),
                    rs.getString("category_name"),
                    parseQuarterPeriod(periodCode),
                    value,
                    rs.getString("unit")
            );
        };
    }

    private LocalDate parseQuarterPeriod(String periodCode) {
        if (periodCode == null || periodCode.length() != 6) return null;
        int year = Integer.parseInt(periodCode.substring(0, 4));
        int quarter = Integer.parseInt(periodCode.substring(4));
        return LocalDate.of(year, Month.of((quarter - 1) * 3 + 1), 1);
    }
}

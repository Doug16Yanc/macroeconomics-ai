package com.example.macroeconomics_ai.infrastructure.sidra;

import com.example.macroeconomics_ai.domain.model.sidra.SidraLaborIndicator;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Component
public class SidraLaborWriter
        implements ItemWriter<List<SidraLaborIndicator>> {

    private static final String UPSERT_SQL = """
        INSERT INTO sidra_observations (
            table_id,
            variable_id,
            variable_name,
            territory_id,
            territory_name,
            period_code,
            period_name,
            classification_id,
            category_id,
            category_name,
            value,
            unit,
            fetched_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (
            table_id,
            variable_id,
            territory_id,
            period_code,
            classification_id,
            category_id
        )
        DO UPDATE SET
            variable_name = EXCLUDED.variable_name,
            territory_name = EXCLUDED.territory_name,
            period_name = EXCLUDED.period_name,
            category_name = EXCLUDED.category_name,
            value = EXCLUDED.value,
            unit = EXCLUDED.unit,
            fetched_at = EXCLUDED.fetched_at
        WHERE sidra_observations.value IS DISTINCT FROM EXCLUDED.value
        """;

    private final JdbcTemplate jdbcTemplate;

    public SidraLaborWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(Chunk<? extends List<SidraLaborIndicator>> chunks) {

        var fetchedAt = Timestamp.from(Instant.now());

        chunks.getItems()
                .stream()
                .flatMap(List::stream)
                .forEach(obs -> jdbcTemplate.update(
                        UPSERT_SQL,
                        obs.tableId(),
                        obs.variableId(),
                        obs.variableName(),
                        obs.territoryId(),
                        obs.territoryName(),
                        obs.periodCode(),
                        obs.periodName(),
                        obs.classificationId(),
                        obs.categoryId(),
                        obs.categoryName(),
                        obs.value(),
                        obs.unit(),
                        fetchedAt
                ));
    }
}
package com.example.macroeconomics_ai.infrastructure.bcb;

import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Component
public class BcbObservationWriter implements ItemWriter<List<MonetaryIndicator>> {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_SQL = """
        INSERT INTO bcb_observations (series_id, obs_date, value, fetched_at)
        VALUES (?, ?, ?, ?)
        ON CONFLICT (series_id, obs_date)
        DO UPDATE SET value = EXCLUDED.value, fetched_at = EXCLUDED.fetched_at
        WHERE bcb_observations.value IS DISTINCT FROM EXCLUDED.value
        """;

    public BcbObservationWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(Chunk<? extends List<MonetaryIndicator>> chunks) {
        var fetchedAt = Timestamp.from(Instant.now());

        chunks.getItems().stream()
                .flatMap(List::stream)
                .forEach(obs -> jdbcTemplate.update(UPSERT_SQL,
                        obs.seriesId(), obs.date(), obs.value(), fetchedAt));
    }
}
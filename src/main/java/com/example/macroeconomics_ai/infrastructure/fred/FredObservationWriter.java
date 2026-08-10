package com.example.macroeconomics_ai.infrastructure.fred;

import com.example.macroeconomics_ai.domain.model.fred.FredObservation;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class FredObservationWriter implements ItemWriter<List<FredObservation>> {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_SQL = """
        INSERT INTO fred_observations (series_id, obs_date, value, fetched_at)
        VALUES (?, ?, ?, ?)
        ON CONFLICT (series_id, obs_date)
        DO UPDATE SET value = EXCLUDED.value, fetched_at = EXCLUDED.fetched_at
        WHERE fred_observations.value IS DISTINCT FROM EXCLUDED.value
        """;

    public FredObservationWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(Chunk<? extends List<FredObservation>> chunks) {
        chunks.getItems().stream()
                .flatMap(List::stream)
                .forEach(obs -> {
                    jdbcTemplate.update(UPSERT_SQL,
                            obs.seriesId(), obs.date(), obs.value(), Timestamp.from(obs.fetchedAt()));

                });
    }
}

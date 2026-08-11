package com.example.macroeconomics_ai.infrastructure.persistence.repository;

import com.example.macroeconomics_ai.domain.model.fred.FredObservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class FredObservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public FredObservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FredObservation> findObservations(String seriesId, LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.query("""
        SELECT series_id, obs_date, value, fetched_at
        FROM fred_observations
        WHERE series_id = ?
          AND obs_date BETWEEN ? AND ?
        ORDER BY obs_date
        """,
                this::mapRow,
                seriesId, startDate, endDate
        );
    }

    public Optional<FredObservation> findLatest(String seriesId) {
        return jdbcTemplate.query("""
        SELECT series_id, obs_date, value, fetched_at
        FROM fred_observations
        WHERE series_id = ?
        ORDER BY obs_date DESC
        LIMIT 1
        """,
                this::mapRow,
                seriesId
        ).stream().findFirst();
    }

    private FredObservation mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FredObservation(
                rs.getString("series_id"),
                rs.getObject("obs_date", LocalDate.class),
                rs.getBigDecimal("value"),
                rs.getTimestamp("fetched_at").toInstant()
        );
    }
}

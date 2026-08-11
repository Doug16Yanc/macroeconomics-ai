package com.example.macroeconomics_ai.infrastructure.persistence.repository;

import com.example.macroeconomics_ai.domain.model.MonetaryIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class BcbObservationRepository {

    private static final String SELECT_SQL = """
        SELECT series_id, obs_date, value
        FROM bcb_observations
        WHERE series_id = ?
          AND obs_date <= CURRENT_DATE
          AND obs_date >= ?
        ORDER BY obs_date
        """;

    private final JdbcTemplate jdbcTemplate;

    public BcbObservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MonetaryIndicator> findObservations(String seriesId, LocalDate start) {
        return jdbcTemplate.query(SELECT_SQL, rowMapper(), seriesId, start);
    }

    private RowMapper<MonetaryIndicator> rowMapper() {
        return (rs, rowNum) -> new MonetaryIndicator(
                "BCB",
                rs.getString("series_id"),
                rs.getObject("obs_date", LocalDate.class),
                rs.getBigDecimal("value")
        );
    }
}

CREATE TABLE bcb_observations (
      series_id  VARCHAR(20)     NOT NULL,
      obs_date   DATE            NOT NULL,
      value      NUMERIC(20,6)   NOT NULL,
      fetched_at TIMESTAMP       NOT NULL,
      PRIMARY KEY (series_id, obs_date)
);

CREATE INDEX idx_bcb_observations_series_date
    ON bcb_observations (series_id, obs_date DESC);
CREATE TABLE fred_observations (
       series_id  VARCHAR(20)     NOT NULL,
       obs_date   DATE            NOT NULL,
       value      NUMERIC(20,4)   NOT NULL,
       fetched_at TIMESTAMP       NOT NULL,
       PRIMARY KEY (series_id, obs_date)
);

CREATE INDEX idx_fred_observations_series_date
    ON fred_observations (series_id, obs_date DESC);
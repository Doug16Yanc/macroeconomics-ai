CREATE TABLE sidra_observations (
    table_id             VARCHAR(20)      NOT NULL,
    variable_id          VARCHAR(20)      NOT NULL,
    variable_name        VARCHAR(500)     NOT NULL,

    territory_id         VARCHAR(20)      NOT NULL,
    territory_name       VARCHAR(200)     NOT NULL,

    period_code          VARCHAR(20)      NOT NULL,
    period_name          VARCHAR(100)     NOT NULL,

    classification_id    VARCHAR(20)      NOT NULL,
    category_id          VARCHAR(20)      NOT NULL,
    category_name        VARCHAR(300)     NOT NULL,

    value                 NUMERIC(20,6)   NOT NULL,
    unit                  VARCHAR(100)     NOT NULL,

    fetched_at            TIMESTAMP       NOT NULL,

    PRIMARY KEY (
                 table_id,
                 variable_id,
                 territory_id,
                 period_code,
                 classification_id,
                 category_id
        )
);

CREATE INDEX idx_sidra_observations_variable_period
    ON sidra_observations (
       variable_id,
       period_code DESC
    );

CREATE INDEX idx_sidra_observations_territory_period
    ON sidra_observations (
       territory_id,
       period_code DESC
    );

CREATE INDEX idx_sidra_observations_category_period
    ON sidra_observations (
       category_id,
       period_code DESC
    );

CREATE INDEX idx_sidra_observations_table_variable
    ON sidra_observations (
       table_id,
       variable_id
    );
CREATE TABLE IF NOT EXISTS risk_source_category
(
    id          SERIAL,
    description TEXT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS risk_source
(
    id                      VARCHAR(255),
    description             TEXT,
    risk_source_category_id SERIAL,
    PRIMARY KEY (id),
    FOREIGN KEY (risk_source_category_id) REFERENCES risk_source_category (id)
);

CREATE TABLE IF NOT EXISTS damaging_event
(
    id                                             VARCHAR(255),
    description                                    TEXT,
    probability_of_occurrence_value                int,
    probability_of_occurrence_description          text,
    damage_severity_value                          int,
    damage_severity_description                    text,
    risk_rating_value                              int,
    probability_of_occurrence_with_tom_value       int,
    probability_of_occurrence_with_tom_description text,
    damage_severity_with_tom_value                 int,
    damage_severity_with_tom_description           text,
    residual_risk_rating_value                     int,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS tom
(
    id          VARCHAR(255),
    description TEXT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS use_case
(
    id          Integer,
    description TEXT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS damaging_event_tom
(
    damaging_event_id VARCHAR(255),
    tom_id            VARCHAR(255),
    FOREIGN KEY (damaging_event_id) REFERENCES damaging_event (id),
    FOREIGN KEY (tom_id) REFERENCES tom (id),
    PRIMARY KEY (damaging_event_id, tom_id)
);

CREATE TABLE IF NOT EXISTS damaging_event_risk_source
(
    risk_source_id    VARCHAR(255),
    damaging_event_id VARCHAR(255),
    FOREIGN KEY (risk_source_id) REFERENCES risk_source (id),
    FOREIGN KEY (damaging_event_id) REFERENCES damaging_event (id),
    PRIMARY KEY (damaging_event_id, risk_source_id)
);

CREATE TABLE IF NOT EXISTS use_case_risk_source
(
    use_case_id    Integer,
    risk_source_id VARCHAR(255),
    FOREIGN KEY (risk_source_id) REFERENCES risk_source (id),
    FOREIGN KEY (use_case_id) REFERENCES use_case (id),
    PRIMARY KEY (use_case_id, risk_source_id)
);

CREATE TABLE IF NOT EXISTS use_case_damaging_event
(
    use_case_id    Integer,
    damaging_event_id VARCHAR(255),
    FOREIGN KEY (damaging_event_id) REFERENCES damaging_event (id),
    FOREIGN KEY (use_case_id) REFERENCES use_case (id)
);
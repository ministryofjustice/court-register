CREATE TABLE court_detail
(
    id                      VARCHAR(6)   NOT NULL PRIMARY KEY,
    court_name              VARCHAR(80)  NOT NULL,
    court_description       VARCHAR(200),
    type                    VARCHAR(3)   NOT NULL,
    active                  BOOLEAN      NOT NULL,
    created_datetime        TIMESTAMP    NOT NULL,
    last_updated_datetime   TIMESTAMP    NOT NULL
);

CREATE TABLE building
(
    id                      SERIAL       PRIMARY KEY,
    court_code              VARCHAR(6)   NOT NULL,
    sub_code                VARCHAR(6)   NULL,
    building_name           VARCHAR(50)  NULL,
    street                  VARCHAR(80)  NULL,
    locality                VARCHAR(80)  NULL,
    town                    VARCHAR(80)  NULL,
    county                  VARCHAR(80)  NULL,
    postcode                VARCHAR(8)   NULL,
    country                 VARCHAR(16)  NULL,
    created_datetime        TIMESTAMP    NOT NULL,
    last_updated_datetime   TIMESTAMP    NOT NULL
);

CREATE TABLE contact
(
    id                      SERIAL       PRIMARY KEY,
    building_id             INTEGER      NOT NULL,
    type                    VARCHAR(5)   NOT NULL,
    detail                  VARCHAR(80)  NULL,
    created_datetime        TIMESTAMP    NOT NULL,
    last_updated_datetime   TIMESTAMP    NOT NULL
);

CREATE TABLE court_type
(
    id          VARCHAR(3)  NOT NULL PRIMARY KEY,
    description VARCHAR(80) NOT NULL
);

ALTER TABLE court_detail ADD FOREIGN KEY (type) REFERENCES court_type(id);
ALTER TABLE building ADD FOREIGN KEY (court_code) REFERENCES court_detail(id);
ALTER TABLE contact ADD FOREIGN KEY (building_id) REFERENCES building(id);

CREATE INDEX court_detail_idx ON court_detail (type);
CREATE UNIQUE INDEX building_udx ON building (court_code, sub_code);


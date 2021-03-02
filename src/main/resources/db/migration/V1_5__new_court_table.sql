CREATE TABLE court_detail
(
    id                    VARCHAR(6)   NOT NULL PRIMARY KEY,
    court_name            VARCHAR(80)  NOT NULL,
    court_description     VARCHAR(200),
    type_code             VARCHAR(3)   NOT NULL,
    BUILDING_NAME         VARCHAR(35)  NULL,
    STREET                VARCHAR(35)  NULL,
    LOCALITY              VARCHAR(35)  NULL,
    TOWN                  VARCHAR(35)  NULL,
    COUNTY                VARCHAR(35)  NULL,
    POSTCODE              VARCHAR(8)   NULL,
    COUNTRY               VARCHAR(16)  NULL,
    TELEPHONE_NUMBER      VARCHAR(35)  NULL,
    FAX_NUMBER            VARCHAR(35)  NULL,
    EMAIL_ADDRESS         VARCHAR(255) NULL,
    CREATED_DATETIME      TIMESTAMP    NOT NULL,
    CREATED_BY_USER_ID    VARCHAR(60)  NOT NULL,
    LAST_UPDATED_DATETIME TIMESTAMP    NOT NULL,
    LAST_UPDATED_USER_ID  VARCHAR(60)  NOT NULL
);

CREATE TABLE court_type
(
    id          VARCHAR(3)  NOT NULL PRIMARY KEY,
    description VARCHAR(80) NOT NULL
);

ALTER TABLE court_detail ADD FOREIGN KEY (type_code) REFERENCES court_type(id);

CREATE INDEX court_detail_idx ON court_detail (type_code);


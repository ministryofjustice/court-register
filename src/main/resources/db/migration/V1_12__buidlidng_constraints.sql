DROP INDEX building_udx;
CREATE UNIQUE INDEX building_udx ON building (sub_code);
-- TODO replace the ALTER statement with the function and constraint below once we have switched to Postgres
-- ALTER table building ADD CHECK(NOT EXISTS (SELECT 1 FROM court c WHERE c.id = sub_code));

-- create function court_code_exists(court_code varchar) returns boolean as
-- $$
-- begin
--     return exists(select 1 from court c where c.id = court_code);
-- end
-- $$ language plpgsql;
-- ALTER table building ADD constraint sub_code_already_exists check(not court_code_exists(sub_code));


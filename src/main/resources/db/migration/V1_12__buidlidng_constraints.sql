DROP INDEX building_udx;
CREATE UNIQUE INDEX building_udx ON building (sub_code);
ALTER table building ADD CHECK(NOT EXISTS (SELECT 1 FROM court c WHERE c.id = sub_code));

-- Only allows one null sub_code per court_code.  Note there is already a unique constraint on sub_code in buliding_udx, but this does not apply to null values.
CREATE UNIQUE INDEX building_subcode_udx ON building (court_code, (sub_code is null)) where sub_code is null;

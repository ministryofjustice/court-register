update COURT set court_description = null where court_description = '';

delete from BUILDING b where sub_code is null and
    building_name  is null and
    street     is null and
    locality    is null and
    town           is null and
    county         is null and
    postcode         is null and
    country  is null
and not exists (SELECT 1 FROM CONTACT c where c.building_id = b.id )
create table text_search as
select distinct c.id, to_tsvector(
        concat_ws(
                ' ', c.id, c.court_name, c.court_description, ct.description, b.sub_code, b.building_name, b.street,
                b.locality, b.town, b.county, b.postcode, regexp_replace(b.postcode, ' ', ''), cn.type, cn.detail,
                regexp_replace(cn.detail, ' ', '')
            )
    ) as tsv
from court c
join court_type ct on c.type = ct.id
join building b on c.id = b.court_code
join contact cn on b.id = cn.building_id
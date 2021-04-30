create view text_search as
select distinct c.id, to_tsvector(
        concat_ws(' ',
            c.id,
            c.court_name,
            c.court_description,
            ct.description,
            coalesce(b.sub_code, ''),
            coalesce(b.building_name, ''),
            coalesce(b.street, ''),
            coalesce(b.locality, ''),
            coalesce(b.town, ''),
            coalesce(b.county, ''),
            coalesce(b.postcode, ''), regexp_replace(coalesce(b.postcode, ''), ' ', ''),
            coalesce(cn.type, ''),
            coalesce(cn.detail, ''), regexp_replace(coalesce(cn.detail, ''), ' ', '')
        )
    ) as tsv
from court c
join court_type ct on c.type = ct.id
left join building b on c.id = b.court_code
left join contact cn on b.id = cn.building_id
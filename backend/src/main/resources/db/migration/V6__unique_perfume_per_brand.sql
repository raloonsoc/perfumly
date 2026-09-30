-- RF-15: a brand cannot have two perfumes with the same name (case-insensitive).
-- Verified beforehand: the full seeded catalog (23846 perfumes) has no (brand_id, lower(name)) duplicates.
CREATE UNIQUE INDEX uq_perfume_brand_name ON perfumes (brand_id, lower(name));

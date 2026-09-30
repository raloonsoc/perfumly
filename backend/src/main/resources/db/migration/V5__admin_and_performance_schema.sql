-- RF-14: NULL = active account; a value = blocked since that instant.
ALTER TABLE users ADD COLUMN blocked_at TIMESTAMP(6) WITH TIME ZONE;

-- RF-15: NULL = visible; a value = hidden from the public catalog since that instant.
ALTER TABLE perfumes ADD COLUMN hidden_at TIMESTAMP(6) WITH TIME ZONE;
CREATE INDEX idx_perfume_visible_name ON perfumes (name) WHERE hidden_at IS NULL;

-- RF-5: favorites are listed newest first. Existing rows get the migration instant.
ALTER TABLE user_favorites ADD COLUMN created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now();
CREATE INDEX idx_userfavorite_user_created ON user_favorites (user_id, created_at DESC);

-- RNF-9: audit trail. Deliberately no foreign keys, so rows survive deletion of actor or target.
CREATE TABLE admin_audit_log (
    id UUID NOT NULL PRIMARY KEY,
    actor_id UUID NOT NULL,
    actor_username VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID,
    target_label VARCHAR(255),
    details JSONB,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

-- RF-1, RF-6, RNF-3
CREATE INDEX idx_review_perfume_created ON reviews (perfume_id, created_at DESC);
CREATE INDEX idx_perfumeaccord_perfume_position ON perfume_accords (perfume_id, position);

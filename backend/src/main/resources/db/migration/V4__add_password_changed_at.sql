-- NULL means the password has never been changed since registration; access tokens
-- issued before any password change are always valid in that case (see JwtAuthenticationFilter).
ALTER TABLE users ADD COLUMN password_changed_at TIMESTAMP(6) WITH TIME ZONE;

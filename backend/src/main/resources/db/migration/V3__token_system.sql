ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE verification_tokens (
                                      id UUID NOT NULL PRIMARY KEY,
                                      user_id UUID NOT NULL,
                                      token VARCHAR(255) NOT NULL UNIQUE,
                                      type VARCHAR(30) NOT NULL,
                                      expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                                      used_at TIMESTAMP(6) WITH TIME ZONE,
                                      created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                                      CONSTRAINT fk_verificationtoken_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_verificationtoken_user_type ON verification_tokens (user_id, type);

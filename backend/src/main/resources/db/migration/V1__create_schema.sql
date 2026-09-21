CREATE TABLE brands (
                        id UUID NOT NULL PRIMARY KEY,
                        name VARCHAR(250) NOT NULL UNIQUE,
                        country VARCHAR(100) NOT NULL,
                        created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                        updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE TABLE perfumes (
                          id UUID NOT NULL PRIMARY KEY,
                          name VARCHAR(250) NOT NULL,
                          brand_id UUID NOT NULL,
                          gender VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'UNISEX')),
                          year INTEGER,
                          created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                          CONSTRAINT fk_perfume_brand FOREIGN KEY (brand_id) REFERENCES brands (id)
);
CREATE INDEX idx_perfume_brand ON perfumes (brand_id);

CREATE TABLE notes (
                       id UUID NOT NULL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE accords (
                         id UUID NOT NULL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE perfume_notes (
                               perfume_id UUID NOT NULL,
                               note_id UUID NOT NULL,
                               type VARCHAR(10) NOT NULL CHECK (type IN ('TOP', 'MIDDLE', 'BASE')),
                               PRIMARY KEY (note_id, perfume_id),
                               CONSTRAINT fk_perfumenote_perfume FOREIGN KEY (perfume_id) REFERENCES perfumes (id) ON DELETE CASCADE,
                               CONSTRAINT fk_perfumenote_note FOREIGN KEY (note_id) REFERENCES notes (id) ON DELETE CASCADE
);
CREATE INDEX idx_perfumenote_perfume ON perfume_notes (perfume_id);

CREATE TABLE perfume_accords (
                                 perfume_id UUID NOT NULL,
                                 accord_id UUID NOT NULL,
                                 position INTEGER NOT NULL,
                                 PRIMARY KEY (accord_id, perfume_id),
                                 CONSTRAINT fk_perfumeaccord_perfume FOREIGN KEY (perfume_id) REFERENCES perfumes (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_perfumeaccord_accord FOREIGN KEY (accord_id) REFERENCES accords (id) ON DELETE CASCADE
);
CREATE INDEX idx_perfumeaccord_perfume ON perfume_accords (perfume_id);

CREATE TABLE users (
                       id UUID NOT NULL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                       updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE TABLE user_favorites (
                                user_id UUID NOT NULL,
                                perfume_id UUID NOT NULL,
                                PRIMARY KEY (perfume_id, user_id),
                                CONSTRAINT fk_userfavorite_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                                CONSTRAINT fk_userfavorite_perfume FOREIGN KEY (perfume_id) REFERENCES perfumes (id) ON DELETE CASCADE
);
CREATE INDEX idx_userfavorite_user ON user_favorites (user_id);

CREATE TABLE reviews (
                         id UUID NOT NULL PRIMARY KEY,
                         perfume_id UUID NOT NULL,
                         user_id UUID NOT NULL,
                         rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 10),
                         description TEXT NOT NULL,
                         created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                         updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                         CONSTRAINT uq_review_user_perfume UNIQUE (user_id, perfume_id),
                         CONSTRAINT fk_review_perfume FOREIGN KEY (perfume_id) REFERENCES perfumes (id) ON DELETE CASCADE,
                         CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_review_perfume ON reviews (perfume_id);
CREATE INDEX idx_review_user ON reviews (user_id);
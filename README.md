<div align="center">

# Perfumly

**A Fragrantica-inspired perfume catalog, reviews and recommendation platform.**

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](backend/pom.xml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](backend/pom.xml)
[![Next.js](https://img.shields.io/badge/Next.js-16-black?logo=next.js&logoColor=white)](frontend/package.json)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white)](backend/compose.yaml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## Overview

Perfumly is a full-stack web application for browsing a perfume catalog, reading and
writing reviews, saving favorites, and discovering new fragrances through a
similarity-based recommender built on olfactory accords.

It is a personal portfolio project, built to demonstrate a production-shaped stack —
relational modeling, migrations, caching, authentication, and a typed frontend — beyond
the tutorial-project level.

## Features

| Feature | Status |
|---|---|
| Catalog browsing (pagination, filters, search) | ✅ Done |
| Perfume detail pages (brand, notes, accords) | ✅ Done |
| Automatic catalog seeding from dataset | ✅ Done |
| JWT authentication (register / login) | ▶️ In progress |
| User reviews & ratings | ⏳ Planned |
| Favorites | ⏳ Planned |
| Accord-based recommender | ⏳ Planned |
| Frontend | ⏳ Planned |
| Production deployment | ⏳ Planned |

## Tech stack

**Backend** — [`backend/`](backend)
- Java 25, Spring Boot 4.1.1, Maven (JAR packaging)
- Spring Web (MVC), Spring Data JPA, Spring Security, Spring Data Redis
- PostgreSQL 18 (Alpine) as the primary database
- Flyway for versioned schema migrations
- Lombok, Spring Boot DevTools

**Frontend** — [`frontend/`](frontend)
- Next.js 16 (App Router) + React 19 + TypeScript
- Tailwind CSS 4
- No Vercel: deployed via Docker on self-managed infrastructure (homelab / VPS)

**Infrastructure**
- Docker Compose: `backend/compose.yaml` for development (Postgres + Redis),
  `compose.prod.yaml` for production (independent, not composed together)
- PostgreSQL, Redis

## Architecture

The backend follows a **package-by-feature** layout rather than a traditional
layered (controller/service/repository) split, keeping each domain self-contained:

```
com.ralonsoc.backend
├── config/     SecurityConfig, etc.
├── common/     GlobalExceptionHandler, ErrorResponse (cross-cutting)
├── seed/       DataSeeder — initial catalog load
├── perfume/    Perfume, Brand, Note, Accord, PerfumeNote, PerfumeAccord + DTOs
├── user/       User, UserFavorite
├── review/     Review
└── auth/       JWT auth (work in progress)
```

### Data model

- `brands`, `perfumes`, `notes`, `accords` — catalog tables
- `perfume_notes`, `perfume_accords` — join tables with a composite key
  (`@EmbeddedId`); `perfume_accords` also stores `position` (relevance order)
- `users` (`role`: USER / ADMIN), `user_favorites`, `reviews`
  (rating 1–10, `UNIQUE(user_id, perfume_id)`)
- UUID primary keys throughout, audit timestamps (`created_at` / `updated_at`) via
  Hibernate (`@CreationTimestamp` / `@UpdateTimestamp`)

Schema evolution is fully managed through versioned Flyway migrations
(`backend/src/main/resources/db/migration`).

### API

All catalog endpoints are public; everything else requires authentication by default
(see `SecurityConfig`).

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/perfumes` | Paginated catalog listing. Supports `gender`, `brandId`, and `search` query params |
| `GET` | `/api/perfumes/{id}` | Full detail for a single perfume (brand, notes, accords) |

Responses use dedicated DTOs — JPA entities are never exposed directly.

## Catalog data source

The catalog is imported from the public Kaggle dataset
[**"Fragrantica.com Fragrance Dataset"**](https://www.kaggle.com/datasets), originally
scraped from [Fragrantica.com](https://www.fragrantica.com) by a third party and
republished on Kaggle. Only objective catalog data is used (brand, notes, accords,
year, gender) — no user reviews or photos from Fragrantica.

| | |
|---|---|
| Perfumes | **23,846** |
| Brands | **1,060** |
| Notes | **1,669** |
| Accords | **84** |

Data is seeded automatically on first startup via `DataSeeder`, a Spring Boot
`ApplicationRunner` that reads the CSV bundled at
`backend/src/main/resources/db/seed/perfumes.csv`. Seeding is idempotent and skipped if
the database already contains data.

> This repository does not claim ownership of the original dataset. If you are the
> rights holder and want it taken down, please open an issue.

## Getting started

**Requirements:** Java 25, Maven (or the bundled `mvnw` wrapper), Docker, Bun.

### Backend

```bash
cd backend
cp .env.example .env   # adjust local credentials
docker compose up -d   # start PostgreSQL and Redis
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

### Frontend

```bash
cd frontend
bun install
bun dev
```

## Roadmap

- [ ] JWT authentication (register / login)
- [ ] Reviews & ratings
- [ ] Favorites
- [ ] Accord-similarity recommender
- [ ] Frontend implementation
- [ ] Production deployment

## License

The source code in this repository is distributed under the [MIT](LICENSE) license.

This license covers the source code only. The imported catalog dataset (brands,
perfumes, notes, accords) comes from a third party — see
[Catalog data source](#catalog-data-source) — and is not covered by this license. No
perfume images are included, for copyright reasons.

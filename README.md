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
| Auth: register / login / logout | ✅ Done |
| Auth: JWT access + refresh tokens (rotation, "remember me") | ✅ Done |
| Auth: email verification | ✅ Done |
| Auth: forgot / reset password | ✅ Done |
| Auth: session revocation on password reset, logout-all | ✅ Done |
| Auth: rate limiting (login, register, forgot-password, refresh) | ✅ Done |
| User reviews & ratings | ✅ Done |
| Favorites | ✅ Done |
| HttpOnly cookie authentication | ✅ Done |
| Frontend: home, catalogue, theming | ✅ Done |
| Frontend: full auth flow (login, register, verify, reset password) | ✅ Done |
| Frontend: reviews & favorites UI | ⏳ Planned |
| Frontend: role-based protected routes (guard ready, no page uses it yet) | ⏳ Planned |
| Accord-based recommender | ⏳ Planned |
| Production deployment | ⏳ Planned |

## Tech stack

**Backend** — [`backend/`](backend)
- Java 25, Spring Boot 4.1.1, Maven (JAR packaging)
- Spring Web (MVC), Spring Data JPA, Spring Security, Spring Data Redis
- PostgreSQL 18 (Alpine) as the primary database
- Redis (AOF persistence) for refresh tokens and rate limiting
- Flyway for versioned schema migrations
- [Resend](https://resend.com) for transactional email (verification, password reset)
- [Bucket4j](https://bucket4j.com) for Redis-backed rate limiting
- Lombok, Spring Boot DevTools

**Frontend** — [`frontend/`](frontend)
- Next.js 16 (App Router) + React 19 + TypeScript
- Tailwind CSS 4, shadcn/ui (`base-luma` style) on Base UI
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
├── config/     SecurityConfig, JwtAuthenticationFilter, RateLimitFilter, RedisConfig
├── common/     GlobalExceptionHandler, ErrorResponse (cross-cutting)
├── seed/       DataSeeder — initial catalog load
├── perfume/    Perfume, Brand, Note, Accord, PerfumeNote, PerfumeAccord + DTOs
├── user/       User (Role: USER/ADMIN), UserFavorite
├── review/     Review
├── email/      EmailService (Resend), HTML templates
└── auth/       Register, login, refresh, logout(-all), email verification,
                password reset — JWT access + Redis-backed refresh tokens
```

### Data model

- `brands`, `perfumes`, `notes`, `accords` — catalog tables
- `perfume_notes`, `perfume_accords` — join tables with a composite key
  (`@EmbeddedId`); `perfume_accords` also stores `position` (relevance order)
- `users` (`role`: USER / ADMIN, `email_verified`, `password_changed_at`),
  `user_favorites`, `reviews` (rating 1–10, `UNIQUE(user_id, perfume_id)`)
- `verification_tokens` — single-use, typed (`EMAIL_VERIFICATION` / `PASSWORD_RESET`),
  independently-expiring tokens backing both email verification and password reset
- Refresh tokens live in Redis, not Postgres — high-churn (rotated on every use)
  session state, not data that needs to be queried or audited
- UUID primary keys throughout, audit timestamps (`created_at` / `updated_at`) via
  Hibernate (`@CreationTimestamp` / `@UpdateTimestamp`)

Schema evolution is fully managed through versioned Flyway migrations
(`backend/src/main/resources/db/migration`).

### API

Catalog reads (`GET /api/perfumes/**`, `/api/brands/**`) and most of `/api/auth/**`
are public; `/api/auth/me` and `/api/auth/logout-all` require a valid session, and
everything outside `/api/auth/**` requires authentication by default (see
`SecurityConfig`).

Authentication is JWT-based with two cookies, both `httpOnly` and `SameSite=Lax`:
`jwt` (short-lived access token) and `refresh_token` (longer-lived, rotated on every
use). Neither is ever exposed in a JSON response body. `POST /api/auth/refresh` reads
the refresh cookie, revokes it and issues a new pair — the frontend does this
automatically when a request comes back `401`. An `Authorization: Bearer <token>`
header (access token only) is accepted as a fallback for Postman/CLI testing; the
cookie takes precedence when both are present. CORS is enabled for
`http://localhost:3000` with `Access-Control-Allow-Credentials: true` so the frontend
can rely on the cookies across origins.

Registration doesn't log the user in — it sends a verification email, and login is
rejected until the account is verified. A password reset invalidates every access
token already issued and revokes every refresh token the account has, ending all of
its sessions, not just the one that made the request.

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/perfumes` | Public | Paginated catalog listing. Supports `gender`, `brandId`, and `search` query params |
| `GET` | `/api/perfumes/{id}` | Public | Full detail for a single perfume (brand, notes, accords) |
| `POST` | `/api/auth/register` | Public | Create a new user account (unverified) and send a verification email |
| `GET` | `/api/auth/verify-email` | Public | Verify the account for the `token` query param |
| `POST` | `/api/auth/login` | Public | Authenticate with credentials (`rememberMe` controls the refresh token's lifetime). Sets both cookies, returns the user's profile |
| `POST` | `/api/auth/refresh` | Public (refresh cookie) | Rotate the refresh token and issue a new access token |
| `POST` | `/api/auth/forgot-password` | Public | Always returns a generic message; emails a reset link only if the account exists |
| `POST` | `/api/auth/reset-password` | Public | Set a new password from a reset token; revokes every session on the account |
| `POST` | `/api/auth/logout` | Public | Revokes the current refresh token, clears both cookies |
| `POST` | `/api/auth/logout-all` | Cookie / Bearer | Revokes every refresh token for the current user (all sessions/devices) |
| `GET` | `/api/auth/me` | Cookie / Bearer | Current authenticated user's profile (includes `role`) |
| `GET` | `/api/perfumes/{perfumeId}/reviews` | Public | Paginated list of reviews for a perfume |
| `POST` | `/api/perfumes/{perfumeId}/reviews` | Cookie / Bearer | Create a review (rating 1–10 + description). `409` if the user already reviewed this perfume |
| `PUT` | `/api/reviews/{id}` | Cookie / Bearer | Update your own review. `403` if it belongs to another user |
| `DELETE` | `/api/reviews/{id}` | Cookie / Bearer | Delete your own review. `403` if it belongs to another user |
| `GET` | `/api/users/me/favorites` | Cookie / Bearer | Paginated list of the current user's favorite perfumes |
| `POST` | `/api/users/me/favorites/{perfumeId}` | Cookie / Bearer | Add a perfume to favorites. `409` if already favorited |
| `DELETE` | `/api/users/me/favorites/{perfumeId}` | Cookie / Bearer | Remove a perfume from favorites. `404` if not favorited |

Responses use dedicated DTOs — JPA entities are never exposed directly.

### Frontend

Next.js App Router with Server Components by default — routes are fetched and
rendered on the server (`app/page.tsx`, `app/perfumes/page.tsx`), and `"use client"`
is reserved for actual interactivity (filters, theme toggle, forms). Catalogue
filter/search state lives in the URL (`searchParams`), not component state, so
results stay shareable and bookmarkable while keeping SSR on first load.

Implemented pages: home, catalogue (pagination, filters, search, brand combobox),
perfume favouriting, light/dark theming, styled 404 / 500 error pages, and the full
auth flow — login (with "remember me"), register, check your email, verify email,
forgot password, reset password. `lib/api.ts`'s `apiFetch` handles token refresh
transparently, so authenticated requests don't need to think about expiration.
Reviews and favorites currently have a backend API but no dedicated UI yet.

## Catalog data source

The catalog is imported from the public Kaggle dataset
[**"Fragrantica.com Fragrance Dataset"**](https://www.kaggle.com/datasets/olgagmiufana1/fragrantica-com-fragrance-dataset/data),
originally scraped from [Fragrantica.com](https://www.fragrantica.com) by a third party
and republished on Kaggle. Only objective catalog data is used (brand, notes, accords,
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
cp .env.example .env   # adjust local credentials, including RESEND_API_KEY/EMAIL_FROM
docker compose up -d   # start PostgreSQL and Redis (Redis runs with AOF persistence)
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`. A Resend API key is required for
registration/password-reset emails to actually send — get one at
[resend.com](https://resend.com); on the free tier, sending is limited to your own
verified address unless you verify a custom domain.

### Frontend

```bash
cd frontend
bun install
bun dev
```

## Roadmap

- [x] Auth: register / login / logout, JWT access + refresh tokens, email
      verification, password reset, rate limiting, remember-me
- [x] Reviews & ratings
- [x] Favorites
- [x] HttpOnly cookie authentication
- [x] Frontend: home, catalogue, theming, full auth flow
- [ ] Frontend: reviews & favorites UI
- [ ] Frontend: role-based protected routes (mechanism ready, unused)
- [ ] Accord-similarity recommender
- [ ] Production deployment

## License

The source code in this repository is distributed under the [MIT](LICENSE) license.

This license covers the source code only. The imported catalog dataset (brands,
perfumes, notes, accords) comes from a third party — see
[Catalog data source](#catalog-data-source) — and is not covered by this license. No
perfume images are included, for copyright reasons.

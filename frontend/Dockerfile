# syntax=docker/dockerfile:1

# --- Dependencies stage ---
FROM oven/bun:1.3-alpine AS deps
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

# --- Build stage ---
FROM oven/bun:1.3-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

ARG NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
RUN bun run build

# --- Runtime stage ---
FROM oven/bun:1.3-alpine AS runtime
RUN addgroup -S nextjs && adduser -S nextjs -G nextjs
USER nextjs
WORKDIR /app

ENV NODE_ENV=production
COPY --from=build --chown=nextjs:nextjs /app/public ./public
COPY --from=build --chown=nextjs:nextjs /app/.next/standalone ./
COPY --from=build --chown=nextjs:nextjs /app/.next/static ./.next/static

EXPOSE 3000
ENTRYPOINT ["bun", "server.js"]

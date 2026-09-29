package com.ralonsoc.backend.auth;

import com.ralonsoc.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

// Refresh tokens live in Redis only (not Postgres): they're short-lived, high-churn
// (rotated on every use) session state, not data anyone needs to query or audit —
// same reasoning as rate-limit counters, unlike verification_tokens which are meant
// to persist reliably. Redis AOF persistence (see compose.yaml) protects against
// losing active sessions on a container restart.
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String TOKEN_KEY_PREFIX = "refresh-token:";
    private static final String USER_TOKENS_KEY_PREFIX = "refresh-tokens-by-user:";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public String issue(User user) {
        String token = SecureTokenGenerator.generate();
        Duration ttl = Duration.ofMillis(refreshExpirationMs);

        redisTemplate.opsForValue().set(tokenKey(token), user.getId().toString(), ttl);
        redisTemplate.opsForSet().add(userTokensKey(user.getId()), token);
        redisTemplate.expire(userTokensKey(user.getId()), ttl);

        return token;
    }

    // Rotation: consumes the old token and issues a brand new one, so a stolen-and-reused
    // old token stops working the moment the legitimate client refreshes first.
    public String rotate(String oldToken, User user) {
        revoke(oldToken);
        return issue(user);
    }

    public UUID getUserId(String token) {
        String userId = redisTemplate.opsForValue().get(tokenKey(token));
        if (userId == null) {
            throw new InvalidTokenException();
        }
        return UUID.fromString(userId);
    }

    public void revoke(String token) {
        String userId = redisTemplate.opsForValue().get(tokenKey(token));
        redisTemplate.delete(tokenKey(token));
        if (userId != null) {
            redisTemplate.opsForSet().remove(userTokensKey(UUID.fromString(userId)), token);
        }
    }

    // Revokes every active refresh token for a user — used on password reset, so a
    // stolen session can't keep renewing itself after the legitimate owner locks it out.
    public void revokeAllForUser(UUID userId) {
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey(userId));
        if (tokens != null && !tokens.isEmpty()) {
            tokens.forEach(token -> redisTemplate.delete(tokenKey(token)));
        }
        redisTemplate.delete(userTokensKey(userId));
    }

    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    private String userTokensKey(UUID userId) {
        return USER_TOKENS_KEY_PREFIX + userId;
    }
}

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
    // Stored value is "<userId>:<rememberMe>" so a rotation (which doesn't get a fresh
    // login request) knows whether to keep re-issuing the short or the long TTL.
    private static final String VALUE_SEPARATOR = ":";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.refresh-expiration-remember-me-ms}")
    private long refreshExpirationRememberMeMs;

    public record IssuedToken(String token, boolean rememberMe) {
    }

    public String issue(User user, boolean rememberMe) {
        String token = SecureTokenGenerator.generate();
        Duration ttl = Duration.ofMillis(rememberMe ? refreshExpirationRememberMeMs : refreshExpirationMs);
        String value = user.getId() + VALUE_SEPARATOR + rememberMe;

        redisTemplate.opsForValue().set(tokenKey(token), value, ttl);
        redisTemplate.opsForSet().add(userTokensKey(user.getId()), token);
        redisTemplate.expire(userTokensKey(user.getId()), ttl);

        return token;
    }

    // Rotation: consumes the old token and issues a brand new one, so a stolen-and-reused
    // old token stops working the moment the legitimate client refreshes first. Carries
    // the original remember-me choice forward (returned alongside the new token, so the
    // caller can size the response cookie the same way) instead of resetting to the short
    // default on every refresh.
    public IssuedToken rotate(String oldToken, User user) {
        boolean rememberMe = Boolean.parseBoolean(parseValue(oldToken)[1]);
        revoke(oldToken);
        return new IssuedToken(issue(user, rememberMe), rememberMe);
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseValue(token)[0]);
    }

    private String[] parseValue(String token) {
        String value = redisTemplate.opsForValue().get(tokenKey(token));
        if (value == null) {
            throw new InvalidTokenException();
        }
        String[] parts = value.split(VALUE_SEPARATOR, 2);
        if (parts.length != 2) {
            throw new InvalidTokenException();
        }
        return parts;
    }

    public void revoke(String token) {
        String value = redisTemplate.opsForValue().get(tokenKey(token));
        redisTemplate.delete(tokenKey(token));
        if (value != null) {
            UUID userId = UUID.fromString(value.split(VALUE_SEPARATOR, 2)[0]);
            redisTemplate.opsForSet().remove(userTokensKey(userId), token);
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

package com.ralonsoc.backend.auth;

import com.ralonsoc.backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String ACCESS_COOKIE_NAME = "jwt";
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.cookie-secure}")
    private boolean cookieSecure;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry =  new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Instant extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt).toInstant();
    }

    // A token is valid if it matches the expected subject, hasn't expired, and — crucially —
    // was issued after the user's last password change. Without that last check, an access
    // token minted before a password reset would keep working until it naturally expires,
    // defeating the point of the reset (see AuthService.resetPassword).
    public boolean isTokenValid(String token, User expectedUser) {
        String email = extractEmail(token);
        boolean matchesUser = email.equals(expectedUser.getEmail());
        boolean notExpired = !isTokenExpired(token);
        boolean issuedAfterPasswordChange = expectedUser.getPasswordChangedAt() == null
                || !extractIssuedAt(token).isBefore(expectedUser.getPasswordChangedAt());
        return matchesUser && notExpired && issuedAfterPasswordChange;
    }

    public ResponseCookie generateAccessCookie(String token) {
        return buildCookie(ACCESS_COOKIE_NAME, token, Duration.ofMillis(expirationMs));
    }

    public ResponseCookie clearAccessCookie() {
        return buildCookie(ACCESS_COOKIE_NAME, "", Duration.ZERO);
    }

    public ResponseCookie generateRefreshCookie(String token) {
        return buildCookie(REFRESH_COOKIE_NAME, token, Duration.ofMillis(refreshExpirationMs));
    }

    public ResponseCookie clearRefreshCookie() {
        return buildCookie(REFRESH_COOKIE_NAME, "", Duration.ZERO);
    }

    public String extractRefreshTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(REFRESH_COOKIE_NAME)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}

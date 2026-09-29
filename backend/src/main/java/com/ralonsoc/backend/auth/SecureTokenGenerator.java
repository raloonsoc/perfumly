package com.ralonsoc.backend.auth;

import java.security.SecureRandom;
import java.util.Base64;

// Shared by VerificationToken-backed flows (email verification, password reset) and
// RefreshTokenService — a cryptographically random, URL-safe string suitable for both
// email links and cookie values.
final class SecureTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecureTokenGenerator() {
    }

    static String generate() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}

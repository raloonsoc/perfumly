package com.ralonsoc.backend.auth;

import com.ralonsoc.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserAndTypeAndUsedAtIsNullAndExpiresAtAfter(User user, TokenType type, Instant now);
}

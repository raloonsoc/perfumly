package com.ralonsoc.backend.auth;

import com.ralonsoc.backend.auth.dto.AuthResponse;
import com.ralonsoc.backend.auth.dto.ForgotPasswordRequest;
import com.ralonsoc.backend.auth.dto.LoginRequest;
import com.ralonsoc.backend.auth.dto.MessageResponse;
import com.ralonsoc.backend.auth.dto.RegisterRequest;
import com.ralonsoc.backend.auth.dto.RegisterResponse;
import com.ralonsoc.backend.auth.dto.ResetPasswordRequest;
import com.ralonsoc.backend.auth.dto.UserProfileResponse;
import com.ralonsoc.backend.email.EmailService;
import com.ralonsoc.backend.user.User;
import com.ralonsoc.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.token.email-verification-expiration-ms}")
    private long emailVerificationExpirationMs;

    @Value("${app.token.password-reset-expiration-ms}")
    private long passwordResetExpirationMs;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyInUseException(request.username());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        sendVerificationEmail(user);

        return new RegisterResponse("Registration successful. Please check your email to verify your account.");
    }

    private void sendVerificationEmail(User user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setType(TokenType.EMAIL_VERIFICATION);
        verificationToken.setToken(SecureTokenGenerator.generate());
        verificationToken.setExpiresAt(Instant.now().plusMillis(emailVerificationExpirationMs));

        verificationTokenRepository.save(verificationToken);

        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
    }

    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(InvalidTokenException::new);

        if (verificationToken.getType() != TokenType.EMAIL_VERIFICATION || verificationToken.getUsedAt() != null) {
            throw new InvalidTokenException();
        }
        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException();
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsedAt(Instant.now());
        verificationTokenRepository.save(verificationToken);
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        // Always return the same generic message, whether the email exists or not —
        // revealing which emails are registered is a user-enumeration vulnerability.
        userRepository.findByEmail(request.email()).ifPresentOrElse(
                this::sendPasswordResetEmail,
                () -> log.info("Password reset requested for an email that doesn't exist: {}", request.email())
        );

        return new MessageResponse("If an account exists for that email, we've sent a password reset link.");
    }

    private void sendPasswordResetEmail(User user) {
        VerificationToken resetToken = new VerificationToken();
        resetToken.setUser(user);
        resetToken.setType(TokenType.PASSWORD_RESET);
        resetToken.setToken(SecureTokenGenerator.generate());
        resetToken.setExpiresAt(Instant.now().plusMillis(passwordResetExpirationMs));

        verificationTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getToken();
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        VerificationToken resetToken = verificationTokenRepository.findByToken(request.token())
                .orElseThrow(InvalidTokenException::new);

        if (resetToken.getType() != TokenType.PASSWORD_RESET || resetToken.getUsedAt() != null) {
            throw new InvalidTokenException();
        }
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException();
        }

        User user = resetToken.getUser();
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        // Rejects any access token already in the wild (JwtService.isTokenValid) and,
        // combined with the revocation below, ends every session the account has open —
        // not just the one making this request.
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        verificationTokenRepository.save(resetToken);

        refreshTokenService.revokeAllForUser(user.getId());

        return new MessageResponse("Your password has been reset. You can now log in with your new password.");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        } catch (DisabledException e) {
            throw new EmailNotVerifiedException();
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User should exist after successful authentication"));

        return buildAuthResponse(user);
    }

    // Reads the refresh token cookie, rotates it (old one revoked, new one issued) and
    // mints a fresh short-lived access token — lets the client stay logged in past the
    // access token's expiration without re-entering credentials.
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new InvalidTokenException();
        }

        var userId = refreshTokenService.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidTokenException::new);

        String rotatedRefreshToken = refreshTokenService.rotate(refreshToken, user);

        String accessToken = jwtService.generateToken(user);
        ResponseCookie accessCookie = jwtService.generateAccessCookie(accessToken);
        ResponseCookie refreshCookie = jwtService.generateRefreshCookie(rotatedRefreshToken);
        UserProfileResponse profile = new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
        return new AuthResponse(profile, accessCookie, refreshCookie);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        ResponseCookie accessCookie = jwtService.generateAccessCookie(accessToken);

        String refreshToken = refreshTokenService.issue(user);
        ResponseCookie refreshCookie = jwtService.generateRefreshCookie(refreshToken);

        UserProfileResponse profile = new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
        return new AuthResponse(profile, accessCookie, refreshCookie);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
    }

    // Revokes every refresh token the user has, on every device/browser — not just the
    // one making this request. The caller must still clear its own cookies afterward,
    // same as a regular logout.
    public void logoutAllSessions(User user) {
        refreshTokenService.revokeAllForUser(user.getId());
    }

    public UserProfileResponse getCurrentUser(User user) {
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}

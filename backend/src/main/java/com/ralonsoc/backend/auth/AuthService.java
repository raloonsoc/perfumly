package com.ralonsoc.backend.auth;

import com.ralonsoc.backend.auth.dto.AuthResponse;
import com.ralonsoc.backend.auth.dto.LoginRequest;
import com.ralonsoc.backend.auth.dto.RegisterRequest;
import com.ralonsoc.backend.auth.dto.RegisterResponse;
import com.ralonsoc.backend.auth.dto.UserProfileResponse;
import com.ralonsoc.backend.email.EmailService;
import com.ralonsoc.backend.user.User;
import com.ralonsoc.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.token.email-verification-expiration-ms}")
    private long emailVerificationExpirationMs;

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
        verificationToken.setToken(generateSecureToken());
        verificationToken.setExpiresAt(Instant.now().plusMillis(emailVerificationExpirationMs));

        verificationTokenRepository.save(verificationToken);

        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(InvalidTokenException::new);

        if (verificationToken.getUsedAt() != null) {
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

        String token = jwtService.generateToken(user);
        ResponseCookie cookie = jwtService.generateCookie(token);
        UserProfileResponse profile = new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
        return new AuthResponse(profile, cookie);
    }

    public ResponseCookie logout() {
        return jwtService.clearCookie();
    }

    public UserProfileResponse getCurrentUser(User user) {
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}

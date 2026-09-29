package com.ralonsoc.backend.auth;

import com.ralonsoc.backend.auth.dto.AuthResponse;
import com.ralonsoc.backend.auth.dto.ForgotPasswordRequest;
import com.ralonsoc.backend.auth.dto.LoginRequest;
import com.ralonsoc.backend.auth.dto.MessageResponse;
import com.ralonsoc.backend.auth.dto.RegisterRequest;
import com.ralonsoc.backend.auth.dto.RegisterResponse;
import com.ralonsoc.backend.auth.dto.ResetPasswordRequest;
import com.ralonsoc.backend.auth.dto.UserProfileResponse;
import com.ralonsoc.backend.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/login")
    public UserProfileResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        var result = authService.login(request);
        addAuthCookies(response, result);
        return result.profile();
    }

    @PostMapping("/refresh")
    public UserProfileResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtService.extractRefreshTokenFromCookie(request.getCookies());
        var result = authService.refresh(refreshToken);
        addAuthCookies(response, result);
        return result.profile();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtService.extractRefreshTokenFromCookie(request.getCookies());
        authService.logout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.clearAccessCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.clearRefreshCookie().toString());
    }

    @PostMapping("/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutAllSessions(@AuthenticationPrincipal User user, HttpServletResponse response) {
        authService.logoutAllSessions(user);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.clearAccessCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.clearRefreshCookie().toString());
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(@AuthenticationPrincipal User user) {
        return authService.getCurrentUser(user);
    }

    private void addAuthCookies(HttpServletResponse response, AuthResponse result) {
        response.addHeader(HttpHeaders.SET_COOKIE, result.accessCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, result.refreshCookie().toString());
    }
}

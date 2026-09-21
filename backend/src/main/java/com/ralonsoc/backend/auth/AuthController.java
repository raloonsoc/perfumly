package com.ralonsoc.backend.auth;

import com.ralonsoc.backend.auth.dto.AuthResponse;
import com.ralonsoc.backend.auth.dto.LoginRequest;
import com.ralonsoc.backend.auth.dto.RegisterRequest;
import com.ralonsoc.backend.auth.dto.UserProfileResponse;
import com.ralonsoc.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(@AuthenticationPrincipal User user) {
        return authService.getCurrentUser(user);
    }
}

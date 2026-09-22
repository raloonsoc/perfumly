package com.ralonsoc.backend.auth;

import com.ralonsoc.backend.auth.dto.AuthResponse;
import com.ralonsoc.backend.auth.dto.LoginRequest;
import com.ralonsoc.backend.auth.dto.RegisterRequest;
import com.ralonsoc.backend.auth.dto.UserProfileResponse;
import com.ralonsoc.backend.user.User;
import com.ralonsoc.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthResponse register(RegisterRequest request) {
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

        String token = jwtService.generateToken(user);
        ResponseCookie cookie = jwtService.generateCookie(token);
        UserProfileResponse profile = new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
        return new AuthResponse(profile, cookie);
    }


    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
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

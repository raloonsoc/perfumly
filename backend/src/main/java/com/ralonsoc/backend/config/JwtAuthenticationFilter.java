package com.ralonsoc.backend.config;

import com.ralonsoc.backend.auth.JwtService;
import com.ralonsoc.backend.user.User;
import com.ralonsoc.backend.user.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.ralonsoc.backend.common.exception.ErrorResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromCookie(request);

        if (token == null) {
            token = extractTokenFromHeader(request); // fallback para Postman/pruebas
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (authenticateFromToken(request, token) == AuthResult.BLOCKED) {
                writeAccountBlocked(request, response);
                return;
            }
        } catch (JwtException e) {
            // Expired, malformed or badly-signed token: treat the request as unauthenticated
            // rather than letting the exception surface as a 500 — an expired access token is
            // an expected condition the frontend recovers from via /api/auth/refresh, not a bug.
            log.debug("Ignoring invalid JWT on {}: {}", request.getRequestURI(), e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private enum AuthResult { AUTHENTICATED, SKIPPED, BLOCKED }

    private AuthResult authenticateFromToken(HttpServletRequest request, String token) {
        String email = jwtService.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            User user = (User) userDetails;
            if (jwtService.isTokenValid(token, user)) {
                if (!user.isAccountNonLocked()) {
                    return AuthResult.BLOCKED;
                }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                return AuthResult.AUTHENTICATED;
            }
        }
        return AuthResult.SKIPPED;
    }

    // Filters run before @RestControllerAdvice, so the error body is written here with the
    // same ErrorResponse shape; `error` carries the machine-readable code (spec.md §4).
    private void writeAccountBlocked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(
                Instant.now(), HttpStatus.FORBIDDEN.value(), "ACCOUNT_BLOCKED",
                "Account blocked", request.getRequestURI()));
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("jwt"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

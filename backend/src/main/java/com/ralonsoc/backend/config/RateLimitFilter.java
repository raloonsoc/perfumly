package com.ralonsoc.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    // Login: brute-force protection, tight window. Register: signup-spam protection, looser.
    // Forgot-password: mirrors login's window — without it, since the response is always a
    // generic 200 (to avoid leaking which emails exist), anyone could spam reset emails at
    // an arbitrary victim's inbox with no feedback loop to stop them.
    private static final Map<String, Supplier<BucketConfiguration>> LIMITED_PATHS = Map.of(
            "/api/auth/login", () -> BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                    .build(),
            "/api/auth/register", () -> BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofHours(1)).build())
                    .build(),
            "/api/auth/forgot-password", () -> BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                    .build(),
            // Looser than login: a legitimate client can hit this whenever its access token
            // expires, but it's still an unauthenticated endpoint guessing/brute-forcing a
            // refresh token value would target.
            "/api/auth/refresh", () -> BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder().capacity(20).refillGreedy(20, Duration.ofMinutes(1)).build())
                    .build()
    );

    private final ProxyManager<byte[]> proxyManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        Supplier<BucketConfiguration> configSupplier = LIMITED_PATHS.get(path);
        if (configSupplier == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = "rate-limit:" + path + ":" + request.getRemoteAddr();
        Bucket bucket = proxyManager.builder().build(key.getBytes(StandardCharsets.UTF_8), configSupplier);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.getWriter().write("Too many requests. Please try again later.");
        }
    }
}

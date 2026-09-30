package com.ralonsoc.backend;

import com.ralonsoc.backend.email.EmailService;
import com.ralonsoc.backend.user.Role;
import com.ralonsoc.backend.user.User;
import com.ralonsoc.backend.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base for integration tests (RNF-8): full context on real Postgres and Redis
 * (Testcontainers, no DB mocks). Only {@link EmailService} is a test double.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    protected static final String DEFAULT_PASSWORD = "Password123!";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UserRepository userRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JdbcTemplate jdbcTemplate;
    @Autowired protected StringRedisTemplate redis;

    @Autowired protected CatalogFixtures catalog;
    @MockitoBean protected EmailService emailService;

    /** Clean state per test: DB (except Flyway history) and Redis (refresh tokens, rate limit). */
    @BeforeEach
    void resetState() {
        jdbcTemplate.execute("""
                DO $$ DECLARE t text; BEGIN
                  FOR t IN SELECT tablename FROM pg_tables
                           WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'
                  LOOP EXECUTE 'TRUNCATE TABLE ' || quote_ident(t) || ' CASCADE'; END LOOP;
                END $$;""");
        redis.execute((org.springframework.data.redis.core.RedisCallback<Void>) c -> {
            c.serverCommands().flushAll();
            return null;
        });
    }

    protected User createVerifiedUser(String email, String username) {
        return createUser(email, username, Role.USER);
    }

    protected User createAdmin(String email, String username) {
        return createUser(email, username, Role.ADMIN);
    }

    private User createUser(String email, String username, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(role);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    /** Performs a real login and returns the {@code jwt} and {@code refresh_token} cookies. */
    protected Cookie[] loginCookies(String email) throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                                Map.of("email", email, "password", DEFAULT_PASSWORD, "rememberMe", false))))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookies();
    }
}

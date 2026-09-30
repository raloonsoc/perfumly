package com.ralonsoc.backend;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthSmokeIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("RNF-8 · verified user logs in and GET /api/auth/me returns 200")
    void verifiedUserCanLoginAndCallMe() throws Exception {
        var user = createVerifiedUser("smoke@test.com", "smokeuser");

        Cookie[] cookies = loginCookies(user.getEmail());

        mockMvc.perform(get("/api/auth/me").cookie(cookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("smoke@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("RNF-8 · admin created by helper exposes role ADMIN in /me")
    void adminHelperCreatesAdmin() throws Exception {
        var admin = createAdmin("admin@test.com", "adminuser");

        mockMvc.perform(get("/api/auth/me").cookie(loginCookies(admin.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("RNF-8 · the EmailService double replaces the real one")
    void emailServiceIsMocked() {
        org.junit.jupiter.api.Assertions.assertTrue(
                org.mockito.Mockito.mockingDetails(emailService).isMock());
    }
}

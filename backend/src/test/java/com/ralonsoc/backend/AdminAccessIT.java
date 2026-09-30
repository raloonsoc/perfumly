package com.ralonsoc.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAccessIT extends AbstractIntegrationTest {

    private static final String ADMIN_ROUTE = "/api/admin/anything";

    @Test
    @DisplayName("RF-12 · anonymous request to /api/admin/** gets 401")
    void anonymousGets401() throws Exception {
        mockMvc.perform(get(ADMIN_ROUTE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RF-12 · USER request to /api/admin/** gets 403")
    void userGets403() throws Exception {
        var user = createVerifiedUser("user@test.com", "plainuser");

        mockMvc.perform(get(ADMIN_ROUTE).cookie(loginCookies(user.getEmail())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RF-12 · ADMIN request to /api/admin/** gets neither 401 nor 403")
    void adminIsNeitherUnauthorizedNorForbidden() throws Exception {
        var admin = createAdmin("admin@test.com", "adminuser");

        int status = mockMvc.perform(get(ADMIN_ROUTE).cookie(loginCookies(admin.getEmail())))
                .andReturn().getResponse().getStatus();

        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }
}

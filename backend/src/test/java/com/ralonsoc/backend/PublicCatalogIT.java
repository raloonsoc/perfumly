package com.ralonsoc.backend;

import com.ralonsoc.backend.perfume.Gender;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicCatalogIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("RF-12 · gender + brand + search are combined, not dropped")
    void combinesGenderBrandAndSearch() throws Exception {
        var target = catalog.perfume("Rose Noir").brand("Alpha", "FR").gender(Gender.FEMALE).build();
        catalog.perfume("Rose Blanc").brand("Alpha", "FR").gender(Gender.MALE).build();      // other gender
        catalog.perfume("Rose Rouge").brand("Beta", "FR").gender(Gender.FEMALE).build();     // other brand
        catalog.perfume("Oud Noir").brand("Alpha", "FR").gender(Gender.FEMALE).build();      // other name

        mockMvc.perform(get("/api/perfumes")
                        .param("gender", "FEMALE")
                        .param("brandId", target.getBrand().getId().toString())
                        .param("search", "rose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Rose Noir"));
    }

    @Test
    @DisplayName("RF-12 · search also respects gender and brand filters individually")
    void searchWithGenderOnly() throws Exception {
        catalog.perfume("Rose Noir").gender(Gender.FEMALE).build();
        catalog.perfume("Rose Blanc").gender(Gender.MALE).build();

        mockMvc.perform(get("/api/perfumes").param("gender", "MALE").param("search", "rose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Rose Blanc")));
    }

    @Test
    @DisplayName("RF-15/RNF-10 · hidden perfumes never appear, not even for an admin session")
    void hiddenPerfumeExcludedEvenForAdmin() throws Exception {
        catalog.perfume("Visible One").build();
        catalog.perfume("Visible Hidden").hidden().build();
        var admin = createAdmin("admin@test.com", "adminuser");
        Cookie[] cookies = loginCookies(admin.getEmail());

        mockMvc.perform(get("/api/perfumes").cookie(cookies))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Visible One"));

        mockMvc.perform(get("/api/perfumes").param("search", "Visible").cookie(cookies))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Visible One"));
    }

    @Test
    @DisplayName("RF-15 · public detail of a hidden perfume is 404, even with an admin session")
    void hiddenPerfumeDetailIs404ForAdmin() throws Exception {
        var hidden = catalog.perfume("Secret").hidden().build();
        var admin = createAdmin("admin2@test.com", "adminuser2");
        Cookie[] cookies = loginCookies(admin.getEmail());

        mockMvc.perform(get("/api/perfumes/" + hidden.getId()).cookie(cookies))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RF-15 · public detail of a visible perfume still returns 200")
    void visiblePerfumeDetailIs200() throws Exception {
        var visible = catalog.perfume("Open").build();

        mockMvc.perform(get("/api/perfumes/" + visible.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Open"));
    }
}

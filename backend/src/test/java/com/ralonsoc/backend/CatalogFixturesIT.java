package com.ralonsoc.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ralonsoc.backend.perfume.Gender;
import com.ralonsoc.backend.perfume.Perfume;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatalogFixturesIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("RNF-8 · 2 perfumes with accords: GET /api/perfumes/{id} returns accords ordered by position")
    void perfumeDetailReturnsAccordsOrderedByPosition() throws Exception {
        Perfume first = catalog.perfume("Sauvage")
                .brand("Dior", "France")
                .gender(Gender.MALE)
                .year(2015)
                .accords("fresh spicy", "amber", "citrus", "woody")
                .build();
        Perfume second = catalog.perfume("Black Opium")
                .brand("Yves Saint Laurent", "France")
                .gender(Gender.FEMALE)
                .accord("vanilla", 1)
                .accord("coffee", 2)
                .accord("white floral", 3)
                .build();

        mockMvc.perform(get("/api/perfumes/{id}", first.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accords.length()").value(4))
                .andExpect(jsonPath("$.accords[0].name").value("fresh spicy"))
                .andExpect(jsonPath("$.accords[0].position").value(1))
                .andExpect(jsonPath("$.accords[1].name").value("amber"))
                .andExpect(jsonPath("$.accords[3].name").value("woody"));

        mockMvc.perform(get("/api/perfumes/{id}", second.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accords[0].name").value("vanilla"))
                .andExpect(jsonPath("$.accords[2].name").value("white floral"));
    }

    @Test
    @DisplayName("RNF-8 · positions are honored even when declared out of order")
    void positionsAreHonoredRegardlessOfInsertionOrder() throws Exception {
        Perfume p = catalog.perfume("Test")
                .brand("Marca", "Spain")
                .accord("woody", 3)
                .accord("citrus", 1)
                .accord("amber", 2)
                .build();

        mockMvc.perform(get("/api/perfumes/{id}", p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accords[0].name").value("citrus"))
                .andExpect(jsonPath("$.accords[1].name").value("amber"))
                .andExpect(jsonPath("$.accords[2].name").value("woody"));
    }

    @Test
    @DisplayName("RNF-8 · brand, notes and accords are reused by name across perfumes")
    void sharedEntitiesAreReusedByName() throws Exception {
        Perfume a = catalog.perfume("A").brand("Dior", "France")
                .topNote("Bergamot").middleNote("Lavender").baseNote("Amber").accords("woody").build();
        Perfume b = catalog.perfume("B").brand("Dior", "France")
                .topNote("Bergamot").accords("woody").build();

        org.junit.jupiter.api.Assertions.assertEquals(a.getBrand().getId(), b.getBrand().getId());
        org.junit.jupiter.api.Assertions.assertEquals(1,
                jdbcTemplate.queryForObject("SELECT count(*) FROM brands", Integer.class));
        org.junit.jupiter.api.Assertions.assertEquals(3,
                jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class));
        org.junit.jupiter.api.Assertions.assertEquals(1,
                jdbcTemplate.queryForObject("SELECT count(*) FROM accords", Integer.class));

        mockMvc.perform(get("/api/perfumes/{id}", a.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes.top[0].name").value("Bergamot"))
                .andExpect(jsonPath("$.notes.middle[0].name").value("Lavender"))
                .andExpect(jsonPath("$.notes.base[0].name").value("Amber"));
    }
}

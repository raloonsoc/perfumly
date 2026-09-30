package com.ralonsoc.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * T-09: the full CSV seed must boot with V6 (unique perfume per brand) applied.
 * Own context (seed enabled), so it does not use the truncating base class.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "app.seed.enabled=true")
class FullSeedIT {

    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("RF-15 · full seed starts without error, V6 applied and no (brand, lower(name)) duplicates")
    void fullSeedBootsWithUniqueIndex() {
        Integer perfumes = jdbcTemplate.queryForObject("SELECT count(*) FROM perfumes", Integer.class);
        assertTrue(perfumes > 20000, "perfumes seeded: " + perfumes);

        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM pg_indexes WHERE indexname = 'uq_perfume_brand_name'", Integer.class));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM (SELECT 1 FROM perfumes GROUP BY brand_id, lower(name) HAVING count(*) > 1) d",
                Integer.class));
    }
}

package com.ralonsoc.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ralonsoc.backend.perfume.Perfume;
import com.ralonsoc.backend.user.User;
import com.ralonsoc.backend.user.UserFavorite;
import com.ralonsoc.backend.user.UserFavoriteRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/** Flyway V5/V6 (T-04…T-09): schema changes required by the admin and favorites features. */
class SchemaMigrationsIT extends AbstractIntegrationTest {

    @Autowired UserFavoriteRepository favoriteRepository;

    private String indexDef(String name) {
        List<String> defs = jdbcTemplate.queryForList(
                "SELECT indexdef FROM pg_indexes WHERE schemaname = 'public' AND indexname = ?",
                String.class, name);
        return defs.isEmpty() ? null : defs.get(0);
    }

    @Test
    @DisplayName("RF-14 · a user with blocked_at is not account-non-locked; without it, is")
    void blockedUserIsLocked() {
        User user = createVerifiedUser("blocked@test.com", "blocked");
        assertTrue(user.isAccountNonLocked());

        user.setBlockedAt(Instant.now());
        User saved = userRepository.saveAndFlush(user);

        User reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(reloaded.getBlockedAt());
        assertFalse(reloaded.isAccountNonLocked());
    }

    @Test
    @DisplayName("RF-15 · partial index of visible perfumes exists (WHERE hidden_at IS NULL)")
    void visiblePerfumesPartialIndexExists() {
        String def = indexDef("idx_perfume_visible_name");
        assertNotNull(def);
        assertTrue(def.contains("WHERE (hidden_at IS NULL)"), def);
    }

    @Test
    @DisplayName("RF-5 · a new favorite has non-null created_at; index (user_id, created_at DESC) exists")
    void favoriteHasCreatedAt() {
        User user = createVerifiedUser("fav@test.com", "fav");
        Perfume perfume = catalog.perfume("Sauvage").brand("Dior", "France").build();

        UserFavorite favorite = new UserFavorite();
        favorite.setUser(user);
        favorite.setPerfume(perfume);
        favoriteRepository.saveAndFlush(favorite);

        Object createdAt = jdbcTemplate.queryForObject(
                "SELECT created_at FROM user_favorites WHERE user_id = ?", Object.class, user.getId());
        assertNotNull(createdAt);

        String def = indexDef("idx_userfavorite_user_created");
        assertNotNull(def);
        assertTrue(def.contains("(user_id, created_at DESC)"), def);
    }

    @Test
    @DisplayName("RNF-9 · admin_audit_log exists and has no foreign keys")
    void auditLogTableHasNoForeignKeys() {
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = 'admin_audit_log'", Integer.class));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.table_constraints "
                        + "WHERE table_schema = 'public' AND table_name = 'admin_audit_log' "
                        + "AND constraint_type = 'FOREIGN KEY'", Integer.class));
    }

    @Test
    @DisplayName("RF-1, RF-6, RNF-3 · review and accord indexes exist")
    void reviewAndAccordIndexesExist() {
        String reviews = indexDef("idx_review_perfume_created");
        assertNotNull(reviews);
        assertTrue(reviews.contains("(perfume_id, created_at DESC)"), reviews);

        String accords = indexDef("idx_perfumeaccord_perfume_position");
        assertNotNull(accords);
        assertTrue(accords.contains("(perfume_id, \"position\")") || accords.contains("(perfume_id, position)"), accords);
    }

    @Test
    @DisplayName("RF-15 · unique index rejects the same perfume name (case-insensitive) within a brand")
    void perfumeNameIsUniquePerBrand() {
        catalog.perfume("Sauvage").brand("Dior", "France").build();

        assertThrows(DataIntegrityViolationException.class,
                () -> catalog.perfume("SAUVAGE").brand("Dior", "France").build());

        // Same name under another brand is allowed.
        catalog.perfume("Sauvage").brand("Clone", "Spain").build();
    }
}

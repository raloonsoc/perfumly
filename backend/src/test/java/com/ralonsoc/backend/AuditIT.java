package com.ralonsoc.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ralonsoc.backend.audit.AuditLog;
import com.ralonsoc.backend.audit.AuditLogRepository;
import com.ralonsoc.backend.audit.AuditService;
import com.ralonsoc.backend.user.User;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuditIT extends AbstractIntegrationTest {

    @Autowired AuditService auditService;
    @Autowired AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("RNF-9 · record stores actor, action, target, details and date")
    void recordStoresAllFields() {
        User admin = createAdmin("admin@test.com", "admin");
        UUID targetId = UUID.randomUUID();

        AuditLog saved = auditService.record(admin, "BLOCK_USER", "USER", targetId, "victim",
                Map.of("reason", "spam"));

        AuditLog row = auditLogRepository.findById(saved.getId()).orElseThrow();
        assertEquals(admin.getId(), row.getActorId());
        assertEquals("admin", row.getActorUsername());
        assertEquals("BLOCK_USER", row.getAction());
        assertEquals("USER", row.getTargetType());
        assertEquals(targetId, row.getTargetId());
        assertEquals("victim", row.getTargetLabel());
        assertEquals("spam", row.getDetails().get("reason"));
        assertNotNull(row.getCreatedAt());
    }

    @Test
    @DisplayName("RNF-9 · the audit row survives deletion of the actor")
    void rowSurvivesActorDeletion() {
        User admin = createAdmin("gone@test.com", "gone");
        AuditLog saved = auditService.record(admin, "HIDE_PERFUME", "PERFUME", UUID.randomUUID(), "Sauvage", null);

        userRepository.delete(admin);
        userRepository.flush();

        AuditLog row = auditLogRepository.findById(saved.getId()).orElseThrow();
        assertEquals(admin.getId(), row.getActorId());
        assertEquals("gone", row.getActorUsername());
    }
}

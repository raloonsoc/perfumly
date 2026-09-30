package com.ralonsoc.backend.audit;

import com.ralonsoc.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Records who did what on which item. Joins the caller's transaction (default REQUIRED),
     * so the audit row is committed or rolled back together with the audited action.
     */
    @Transactional
    public AuditLog record(User actor, String action, String targetType, UUID targetId,
                           String targetLabel, Map<String, Object> details) {
        AuditLog log = new AuditLog();
        log.setActorId(actor.getId());
        log.setActorUsername(actor.getUsername());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetLabel(targetLabel);
        log.setDetails(details);
        return auditLogRepository.save(log);
    }
}

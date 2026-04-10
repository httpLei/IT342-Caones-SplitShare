package edu.cit.caones.splitshare.adapter;

import edu.cit.caones.splitshare.dto.response.AdminAuditLogDto;
import edu.cit.caones.splitshare.entity.AdminAuditLog;
import org.springframework.stereotype.Component;

/**
 * Adapter for converting AdminAuditLog entity to AdminAuditLogDto.
 * Centralizes mapping logic and makes it reusable across layers.
 */
@Component
public class AdminAuditLogDtoAdapter {

    /**
     * Adapts an AdminAuditLog entity to a DTO for API response.
     */
    public AdminAuditLogDto adapt(AdminAuditLog log) {
        return AdminAuditLogDto.builder()
                .id(log.getId())
                .action(log.getAction())
                .actorEmail(log.getActorEmail())
                .targetUserId(log.getTargetUserId())
                .targetUserEmail(log.getTargetUserEmail())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt() != null ? log.getCreatedAt().toString() : null)
                .build();
    }
}

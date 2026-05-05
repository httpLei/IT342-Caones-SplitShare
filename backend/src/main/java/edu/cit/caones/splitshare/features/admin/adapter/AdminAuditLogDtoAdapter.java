package edu.cit.caones.splitshare.features.admin.adapter;

import edu.cit.caones.splitshare.shared.dto.response.AdminAuditLogDto;
import edu.cit.caones.splitshare.features.admin.entity.AdminAuditLog;
import org.springframework.stereotype.Component;

@Component
public class AdminAuditLogDtoAdapter {

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

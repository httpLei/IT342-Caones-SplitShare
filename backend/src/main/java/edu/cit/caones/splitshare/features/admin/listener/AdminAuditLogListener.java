package edu.cit.caones.splitshare.features.admin.listener;

import edu.cit.caones.splitshare.features.admin.entity.AdminAuditLog;
import edu.cit.caones.splitshare.shared.event.UserStatusChangedEvent;
import edu.cit.caones.splitshare.features.admin.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuditLogListener {

    private final AdminAuditLogRepository adminAuditLogRepository;

    @EventListener
    public void onUserStatusChanged(UserStatusChangedEvent event) {
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .action(event.getAction())
                .actorEmail(event.getActorEmail())
                .targetUserId(event.getUser().getId())
                .targetUserEmail(event.getUser().getEmail())
                .details(event.getDetails())
                .build());
    }
}

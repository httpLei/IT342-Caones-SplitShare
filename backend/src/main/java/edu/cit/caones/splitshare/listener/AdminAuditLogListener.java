package edu.cit.caones.splitshare.listener;

import edu.cit.caones.splitshare.entity.AdminAuditLog;
import edu.cit.caones.splitshare.event.UserStatusChangedEvent;
import edu.cit.caones.splitshare.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer/Listener for user status change events.
 * Decouples audit logging from core business logic via event-driven architecture.
 */
@Component
@RequiredArgsConstructor
public class AdminAuditLogListener {

    private final AdminAuditLogRepository adminAuditLogRepository;

    /**
     * Listen for user status changes and persist audit log entries.
     */
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

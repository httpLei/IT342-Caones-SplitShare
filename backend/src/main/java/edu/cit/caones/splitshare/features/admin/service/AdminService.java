package edu.cit.caones.splitshare.features.admin.service;

import edu.cit.caones.splitshare.features.admin.adapter.AdminAuditLogDtoAdapter;
import edu.cit.caones.splitshare.features.admin.adapter.AdminUserDtoAdapter;
import edu.cit.caones.splitshare.shared.dto.response.AdminAuditLogDto;
import edu.cit.caones.splitshare.shared.dto.response.AdminUserDto;

import edu.cit.caones.splitshare.features.auth.entity.User;
import edu.cit.caones.splitshare.shared.event.UserStatusChangedEvent;
import edu.cit.caones.splitshare.features.admin.repository.AdminAuditLogRepository;
import edu.cit.caones.splitshare.features.auth.repository.UserRepository;
import edu.cit.caones.splitshare.shared.strategy.UserStatusActionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final AdminUserDtoAdapter adminUserDtoAdapter;
    private final AdminAuditLogDtoAdapter auditLogDtoAdapter;
    private final List<UserStatusActionStrategy> statusStrategies;
    private final ApplicationEventPublisher eventPublisher;

    public List<AdminUserDto> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(adminUserDtoAdapter::adapt)
                .toList();
    }

    public AdminUserDto updateUserStatus(Long userId, boolean enabled, String actorEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        if (user.getEmail().equalsIgnoreCase(actorEmail) && !enabled) {
            throw new IllegalArgumentException("You cannot suspend your own account.");
        }

        user.setEnabled(enabled);
        userRepository.save(user);

        UserStatusActionStrategy strategy = statusStrategies.stream()
                .filter(s -> s.supports(enabled))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for status: " + enabled));

        eventPublisher.publishEvent(
                new UserStatusChangedEvent(this, actorEmail, user, strategy.getAction(), strategy.getDetails())
        );

        return adminUserDtoAdapter.adapt(user);
    }

    public List<AdminAuditLogDto> listAuditLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));

        return adminAuditLogRepository
                .findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(auditLogDtoAdapter::adapt)
                .toList();
    }
}

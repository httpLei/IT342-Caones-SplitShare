package edu.cit.caones.splitshare.service;

import edu.cit.caones.splitshare.dto.response.AdminAuditLogDto;
import edu.cit.caones.splitshare.dto.response.AdminUserDto;
import edu.cit.caones.splitshare.entity.AdminAuditLog;
import edu.cit.caones.splitshare.entity.Role;
import edu.cit.caones.splitshare.entity.User;
import edu.cit.caones.splitshare.repository.AdminAuditLogRepository;
import edu.cit.caones.splitshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public List<AdminUserDto> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toAdminUserDto)
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

        String action = enabled ? "USER_REACTIVATED" : "USER_SUSPENDED";
        String details = enabled
                ? "Admin reactivated user account"
                : "Admin suspended user account";

        adminAuditLogRepository.save(AdminAuditLog.builder()
                .action(action)
                .actorEmail(actorEmail)
                .targetUserId(user.getId())
                .targetUserEmail(user.getEmail())
                .details(details)
                .build());

        return toAdminUserDto(user);
    }

    public List<AdminAuditLogDto> listAuditLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));

        return adminAuditLogRepository
                .findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::toAuditDto)
                .toList();
    }

    private AdminUserDto toAdminUserDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }

    private AdminAuditLogDto toAuditDto(AdminAuditLog log) {
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

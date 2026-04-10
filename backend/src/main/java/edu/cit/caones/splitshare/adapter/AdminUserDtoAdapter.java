package edu.cit.caones.splitshare.adapter;

import edu.cit.caones.splitshare.dto.response.AdminUserDto;
import edu.cit.caones.splitshare.entity.User;
import org.springframework.stereotype.Component;

/**
 * Adapter for converting User entity to AdminUserDto.
 * Centralizes mapping logic and makes it reusable across controllers/services.
 */
@Component
public class AdminUserDtoAdapter {

    /**
     * Adapts a User entity to an AdminUserDto for API response.
     */
    public AdminUserDto adapt(User user) {
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
}

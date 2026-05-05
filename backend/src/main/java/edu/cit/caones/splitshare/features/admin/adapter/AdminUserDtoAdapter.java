package edu.cit.caones.splitshare.features.admin.adapter;

import edu.cit.caones.splitshare.shared.dto.response.AdminUserDto;
import edu.cit.caones.splitshare.features.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AdminUserDtoAdapter {

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

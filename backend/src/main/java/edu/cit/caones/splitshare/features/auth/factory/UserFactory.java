package edu.cit.caones.splitshare.features.auth.factory;

import edu.cit.caones.splitshare.shared.dto.request.RegisterRequest;
import edu.cit.caones.splitshare.features.auth.entity.Role;
import edu.cit.caones.splitshare.features.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for creating User entities with consistent defaults.
 * Centralizes user creation logic and reduces duplication.
 */
@Component
@RequiredArgsConstructor
public class UserFactory {

    /**
     * Creates a User entity from a registration request with encoded password.
     * Applies default values (role, enabled status) consistently.
     */
    public User createFromRegisterRequest(RegisterRequest request, String encodedPassword) {
        return User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
    }
}

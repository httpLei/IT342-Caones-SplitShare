package edu.cit.caones.splitshare.features.auth.service;

import edu.cit.caones.splitshare.features.auth.entity.Role;
import edu.cit.caones.splitshare.features.auth.entity.User;
import edu.cit.caones.splitshare.features.auth.repository.UserRepository;
import edu.cit.caones.splitshare.shared.dto.response.AuthData;
import edu.cit.caones.splitshare.shared.dto.response.UserDto;
import edu.cit.caones.splitshare.shared.exception.AccountDisabledException;
import edu.cit.caones.splitshare.shared.exception.InvalidCredentialsException;
import edu.cit.caones.splitshare.shared.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthData loginWithGoogle(String email, String firstName, String lastName) {
        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException("Google account did not provide an email address");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .firstname(firstName)
                        .lastname(lastName)
                        .email(email)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(Role.ROLE_USER)
                        .enabled(true)
                        .currency("PHP")
                        .build()));

        if (!user.isEnabled()) {
            throw new AccountDisabledException("Your account is suspended. Please contact an administrator.");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthData.builder()
                .user(toUserDto(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .role(user.getRole().name())
                .currency(user.getCurrency())
                .build();
    }
}

package edu.cit.caones.splitshare.features.auth.service;

import edu.cit.caones.splitshare.shared.dto.request.LoginRequest;
import edu.cit.caones.splitshare.shared.dto.request.RegisterRequest;
import edu.cit.caones.splitshare.shared.dto.response.AuthData;
import edu.cit.caones.splitshare.shared.dto.response.UserDto;
import edu.cit.caones.splitshare.features.auth.entity.User;
import edu.cit.caones.splitshare.shared.exception.AccountDisabledException;
import edu.cit.caones.splitshare.shared.exception.DuplicateEmailException;
import edu.cit.caones.splitshare.shared.exception.InvalidCredentialsException;
import edu.cit.caones.splitshare.features.auth.factory.UserFactory;
import edu.cit.caones.splitshare.features.auth.repository.UserRepository;
import edu.cit.caones.splitshare.shared.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserFactory userFactory;

    public AuthData register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateEmailException("Email is already in use");
        }

        User user = userFactory.createFromRegisterRequest(request, passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthData.builder()
                .user(toUserDto(user, false))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthData login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (DisabledException ex) {
            throw new AccountDisabledException("Your account is suspended. Please contact an administrator.");
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Email or password is incorrect");
        }

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email or password is incorrect"));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthData.builder()
                .user(toUserDto(user, true))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private UserDto toUserDto(User user, boolean includeRole) {
        return UserDto.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .role(includeRole ? user.getRole().name() : null)
                .build();
    }
}

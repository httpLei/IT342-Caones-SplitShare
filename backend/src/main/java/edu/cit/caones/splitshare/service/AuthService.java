package edu.cit.caones.splitshare.service;

import edu.cit.caones.splitshare.dto.request.LoginRequest;
import edu.cit.caones.splitshare.dto.request.RegisterRequest;
import edu.cit.caones.splitshare.dto.response.AuthData;
import edu.cit.caones.splitshare.dto.response.UserDto;
import edu.cit.caones.splitshare.entity.Role;
import edu.cit.caones.splitshare.entity.User;
import edu.cit.caones.splitshare.exception.AccountDisabledException;
import edu.cit.caones.splitshare.exception.DuplicateEmailException;
import edu.cit.caones.splitshare.exception.InvalidCredentialsException;
import edu.cit.caones.splitshare.repository.UserRepository;
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

    public AuthData register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already in use");
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        String accessToken  = jwtService.generateToken(user);
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

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email or password is incorrect"));

        String accessToken  = jwtService.generateToken(user);
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

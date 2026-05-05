package edu.cit.caones.splitshare.features.auth.service;

import edu.cit.caones.splitshare.features.auth.entity.Role;
import edu.cit.caones.splitshare.features.auth.entity.User;
import edu.cit.caones.splitshare.features.auth.factory.UserFactory;
import edu.cit.caones.splitshare.features.auth.repository.UserRepository;
import edu.cit.caones.splitshare.shared.dto.request.LoginRequest;
import edu.cit.caones.splitshare.shared.dto.request.RegisterRequest;
import edu.cit.caones.splitshare.shared.dto.response.AuthData;
import edu.cit.caones.splitshare.shared.exception.AccountDisabledException;
import edu.cit.caones.splitshare.shared.exception.DuplicateEmailException;
import edu.cit.caones.splitshare.shared.exception.InvalidCredentialsException;
import edu.cit.caones.splitshare.shared.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserFactory userFactory;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("newuser@example.com");
        validRegisterRequest.setPassword("Password123");
        validRegisterRequest.setFirstname("John");
        validRegisterRequest.setLastname("Doe");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("existing@example.com");
        validLoginRequest.setPassword("Password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("existing@example.com");
        testUser.setFirstname("John");
        testUser.setLastname("Doe");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.ROLE_USER);
        testUser.setEnabled(true);
        testUser.setCreatedAt(Instant.now());
    }

    // ── REGISTRATION TESTS ───────────────────────────────────────────────────

    @Test
    @DisplayName("Should successfully register new user with valid credentials")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(validRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(userFactory.createFromRegisterRequest(eq(validRegisterRequest), anyString())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshToken");

        // Act
        AuthData result = authService.register(validRegisterRequest);

        // Assert
        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        assertNotNull(result.getUser());
        assertEquals("existing@example.com", result.getUser().getEmail());

        verify(userRepository).existsByEmailIgnoreCase(validRegisterRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void testRegisterDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(validRegisterRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> authService.register(validRegisterRequest));
        verify(userRepository).existsByEmailIgnoreCase(validRegisterRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should encode password before saving user")
    void testRegisterEncodesPassword() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(validRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(userFactory.createFromRegisterRequest(eq(validRegisterRequest), eq("encodedPassword"))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("token");

        // Act
        authService.register(validRegisterRequest);

        // Assert
        verify(passwordEncoder).encode(validRegisterRequest.getPassword());
    }

    // ── LOGIN TESTS ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(validLoginRequest.getEmail(), validLoginRequest.getPassword()));
        when(userRepository.findByEmailIgnoreCase(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshToken");

        // Act
        AuthData result = authService.login(validLoginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        assertEquals("existing@example.com", result.getUser().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmailIgnoreCase(validLoginRequest.getEmail());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException on bad credentials")
    void testLoginInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(validLoginRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    @DisplayName("Should throw AccountDisabledException when account is suspended")
    void testLoginSuspendedAccount() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User is disabled"));

        // Act & Assert
        assertThrows(AccountDisabledException.class, () -> authService.login(validLoginRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user not found")
    void testLoginUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(validLoginRequest.getEmail(), validLoginRequest.getPassword()));
        when(userRepository.findByEmailIgnoreCase(validLoginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(validLoginRequest));
        verify(userRepository).findByEmailIgnoreCase(validLoginRequest.getEmail());
    }

    @Test
    @DisplayName("Should generate both access and refresh tokens on successful login")
    void testLoginGeneratesTokens() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(validLoginRequest.getEmail(), validLoginRequest.getPassword()));
        when(userRepository.findByEmailIgnoreCase(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("accessToken123");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshToken123");

        // Act
        AuthData result = authService.login(validLoginRequest);

        // Assert
        assertEquals("accessToken123", result.getAccessToken());
        assertEquals("refreshToken123", result.getRefreshToken());
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
    }

    @Test
    @DisplayName("Should include user role in login response")
    void testLoginIncludesUserRole() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(validLoginRequest.getEmail(), validLoginRequest.getPassword()));
        when(userRepository.findByEmailIgnoreCase(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("token");

        // Act
        AuthData result = authService.login(validLoginRequest);

        // Assert
        assertNotNull(result.getUser().getRole());
        assertEquals("ROLE_USER", result.getUser().getRole());
    }
}

package edu.cit.caones.splitshare.features.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.caones.splitshare.features.auth.entity.Role;
import edu.cit.caones.splitshare.features.auth.entity.User;
import edu.cit.caones.splitshare.features.auth.repository.UserRepository;
import edu.cit.caones.splitshare.shared.dto.request.LoginRequest;
import edu.cit.caones.splitshare.shared.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
    }

    // ── REGISTRATION ENDPOINT TESTS ──────────────────────────────────────────

    @Test
    @DisplayName("Should successfully register new user")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("Password123");
        request.setFirstname("John");
        request.setLastname("Doe");

        mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("Should reject duplicate email on registration")
    void testRegisterDuplicateEmail() throws Exception {
        User existingUser = new User();
        existingUser.setEmail("duplicate@example.com");
        existingUser.setFirstname("John");
        existingUser.setLastname("Doe");
        existingUser.setPassword(passwordEncoder.encode("Password123"));
        existingUser.setRole(Role.ROLE_USER);
        existingUser.setEnabled(true);
        existingUser.setCreatedAt(Instant.now());
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@example.com");
        request.setPassword("Password123");
        request.setFirstname("Jane");
        request.setLastname("Smith");

        mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should reject registration with missing email")
    void testRegisterMissingEmail() throws Exception {
        String invalidJson = "{\"password\": \"Password123\"}";

        mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject weak password on registration")
    void testRegisterWeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("weak");
        request.setFirstname("John");
        request.setLastname("Doe");

        mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should persist user to database after registration")
    void testRegisterPersistsUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("persist@example.com");
        request.setPassword("Password123");
        request.setFirstname("John");
        request.setLastname("Doe");

        mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        assertTrue(userRepository.existsByEmailIgnoreCase("persist@example.com"));
    }

    @Test
    @DisplayName("Should return valid JWT tokens")
    void testRegisterReturnsTokens() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("tokens@example.com");
        request.setPassword("Password123");
        request.setFirstname("John");
        request.setLastname("Doe");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.accessToken", notNullValue()))
            .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("accessToken"));
        assertTrue(response.contains("refreshToken"));
    }

    // ── LOGIN ENDPOINT TESTS ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() throws Exception {
        User testUser = new User();
        testUser.setEmail("login@example.com");
        testUser.setFirstname("John");
        testUser.setLastname("Doe");
        testUser.setPassword(passwordEncoder.encode("Password123"));
        testUser.setRole(Role.ROLE_USER);
        testUser.setEnabled(true);
        testUser.setCreatedAt(Instant.now());
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/v1/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.email").value("login@example.com"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("Should reject login with incorrect password")
    void testLoginIncorrectPassword() throws Exception {
        User testUser = new User();
        testUser.setEmail("wrong@example.com");
        testUser.setFirstname("John");
        testUser.setLastname("Doe");
        testUser.setPassword(passwordEncoder.encode("Password123"));
        testUser.setRole(Role.ROLE_USER);
        testUser.setEnabled(true);
        testUser.setCreatedAt(Instant.now());
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrong@example.com");
        loginRequest.setPassword("WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login for non-existent user")
    void testLoginNonExistentUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/v1/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login for suspended user")
    void testLoginSuspendedUser() throws Exception {
        User suspendedUser = new User();
        suspendedUser.setEmail("suspended@example.com");
        suspendedUser.setFirstname("John");
        suspendedUser.setLastname("Doe");
        suspendedUser.setPassword(passwordEncoder.encode("Password123"));
        suspendedUser.setRole(Role.ROLE_USER);
        suspendedUser.setEnabled(false);
        suspendedUser.setCreatedAt(Instant.now());
        userRepository.save(suspendedUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("suspended@example.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/v1/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should include user role in login response")
    void testLoginIncludesRole() throws Exception {
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstname("Admin");
        adminUser.setLastname("User");
        adminUser.setPassword(passwordEncoder.encode("Password123"));
        adminUser.setRole(Role.ROLE_ADMIN);
        adminUser.setEnabled(true);
        adminUser.setCreatedAt(Instant.now());
        userRepository.save(adminUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/v1/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should maintain consistency between register and login")
    void testRegisterLoginConsistency() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("consistent@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setFirstname("John");
        registerRequest.setLastname("Doe");

        mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("consistent@example.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/v1/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.email").value("consistent@example.com"));
    }
}
